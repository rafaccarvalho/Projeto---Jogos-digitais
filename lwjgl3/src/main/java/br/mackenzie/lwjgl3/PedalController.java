package br.mackenzie.lwjgl3; // PACOTE AJUSTADO PARA ONDE O ARQUIVO ESTÁ LOCALIZADO

import br.mackenzie.input.IPedalController; // Interface do módulo core
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;

import com.fazecast.jSerialComm.SerialPort;

// IMPLEMENTAÇÃO DA INTERFACE
public class PedalController implements IPedalController {

    // ====== ENTRADAS ======
    private Controller controller;           // Joystick
    private SerialPort serialPort;           // Serial USB

    private int axisIndex = -1;              // Eixo do joystick
    private float pedalValue = 0f;           // Valor normalizado 0..1

    // Deadzone
    private final float DEADZONE = 0.05f;

    // Teclado fallback (velocidade contínua)
    private final float KEY_INC = 0.03f;

    // Impulso por pedalada
    private final float PEDAL_IMPULSE = 1.0f;

    public PedalController() {
        detectJoystick();
        detectSerial();
    }

    // ==============================
    //   DETECÇÃO DE JOYSTICK
    // ==============================
    private void detectJoystick() {
        if (Controllers.getControllers().size > 0) {
            controller = Controllers.getControllers().first();
            Gdx.app.log("PedalController", "Joystick encontrado: " + controller.getName());

            int axes = controller.getAxisCount();
            for (int i = 0; i < axes; i++) {
                float v = controller.getAxis(i);
                if (!Float.isNaN(v)) {
                    axisIndex = i;
                    Gdx.app.log("PedalController", "Assumindo axis " + axisIndex + " como pedal.");
                    break;
                }
            }
        }
    }

    // ==============================
    //   DETECÇÃO DE PORTA SERIAL
    // ==============================
    private void detectSerial() {
        SerialPort[] ports = SerialPort.getCommPorts();

        if (ports.length == 0) {
            Gdx.app.log("PedalController", "Nenhuma porta serial encontrada.");
            return;
        }

        // Pega a primeira porta automaticamente
        serialPort = ports[0];
        serialPort.setBaudRate(9600);

        if (serialPort.openPort()) {
            Gdx.app.log("PedalController",
                "Serial conectada: " + serialPort.getSystemPortName());
        } else {
            serialPort = null;
            Gdx.app.log("PedalController", "Falha ao abrir porta serial.");
        }
    }

    // ==============================
    //   LEITURA DO PEDAL
    // ==============================
    @Override // Implementação da interface
    public void update(float delta) {
        boolean updated = false;

        // 1) JOYSTICK
        if (controller != null && axisIndex >= 0) {
            float raw = controller.getAxis(axisIndex);
            float norm = (raw + 1f) / 2f;
            if (Math.abs(norm) < DEADZONE) norm = 0f;

            pedalValue = clamp(norm);
            updated = true;
        }

        // 2) SERIAL (caso exista)
        if (!updated && serialPort != null && serialPort.bytesAvailable() > 0) {
            try {
                byte[] buffer = new byte[32];
                int read = serialPort.readBytes(buffer, buffer.length);

                if (read > 0) {
                    String s = new String(buffer).trim();

                    // Ex: valor "0-1023"
                    int val = Integer.parseInt(s.replaceAll("[^0-9]", ""));
                    float norm = val / 1023f;
                    pedalValue = clamp(norm);
                    updated = true;
                }
            } catch (Exception e) {
                Gdx.app.log("PedalController", "Erro lendo serial: " + e.getMessage());
            }
        }

        // 3) Fallback pelo teclado (aceleração contínua)
        if (!updated) {

            // AGORA USA RIGHT/D para ACELERAR (simular pedalada)
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                Gdx.input.isKeyPressed(Input.Keys.D)) {

                pedalValue = clamp(pedalValue + KEY_INC);
            } else {
                // Desacelera se nenhuma tecla de aceleração for pressionada
                pedalValue = clamp(pedalValue * 0.95f);
            }

            // ADICIONADO: LEFT/A zera a velocidade para frear/parar
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.A)) {
                pedalValue = 0f;
            }
        }

        // 4) **Pedalada por tecla (IMPULSO ÚNICO)**
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT)) {

            pedalValue = clamp(pedalValue + PEDAL_IMPULSE);
        }
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    @Override // Implementação da interface
    public float getPedalValue() {
        return pedalValue;
    }

    @Override // Implementação da interface
    public void dispose() {
        if (serialPort != null)
            serialPort.closePort();
    }
}

// NO MÓDULO CORE: br.mackenzie.input.IPedalController.java

package br.mackenzie.input;

/**
 * Interface para isolar a lógica de entrada (Pedal/Joystick/Serial)
 * da plataforma Desktop, permitindo que o módulo CORE do jogo
 * dependa apenas da funcionalidade (e não da implementação).
 */
public interface IPedalController {

    /** * Atualiza a leitura do pedal/joystick em cada frame.
     * @param delta Tempo passado desde o último frame.
     */
    void update(float delta);

    /** * Retorna o valor normalizado de aceleração (0.0 para parado, 1.0 para velocidade máxima).
     */
    float getPedalValue();

    /** * Libera recursos de hardware (como a porta serial) quando a tela for fechada.
     */
    void dispose();
}

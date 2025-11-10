package br.mackenzie.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HUD {
    public final BitmapFont fonteVida;
    private final BitmapFont fontePontuacao;

    public HUD() {
        fonteVida = new BitmapFont();
        fonteVida.setColor(Color.WHITE);
        fonteVida.getData().setScale(1.5f);

        fontePontuacao = new BitmapFont();
        fontePontuacao.setColor(Color.YELLOW);
        fontePontuacao.getData().setScale(1.5f);
    }

    // --- HUD fixo na tela (usando hudCamera) ---
    public void drawHUD(SpriteBatch batch, ShapeRenderer shapeRenderer,
                        Viewport viewport, OrthographicCamera hudCamera,
                        int vida, int vidaMax, int pontuacao) {

        // --- Teste de fundo (retângulo visível para debug) ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.4f); // fundo semi-transparente
        shapeRenderer.rect(10, 10, 300, 100);
        shapeRenderer.end();

        // --- Coordenadas base ---
        float barX = 30;
        float barY = 70;
        float barWidth = 200;
        float barHeight = 20;

        // --- Barra de fundo ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // --- Barra de vida ---
        float vidaPercentual = (float) vida / vidaMax;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth * vidaPercentual, barHeight);
        shapeRenderer.end();

        // --- Textos ---
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        fonteVida.draw(batch, "Vida: " + vida + "/" + vidaMax, barX, barY + 35);
        fontePontuacao.draw(batch, "Queijos: " + pontuacao, barX, barY - 10);
        batch.end();
    }



    public void dispose() {
        fonteVida.dispose();
        fontePontuacao.dispose();
    }
}

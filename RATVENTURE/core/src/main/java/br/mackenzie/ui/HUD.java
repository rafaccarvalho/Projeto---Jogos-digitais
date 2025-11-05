package br.mackenzie.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HUD {
    private final BitmapFont fonteVida;
    private final BitmapFont fontePontuacao;

    public HUD() {
        fonteVida = new BitmapFont();
        fonteVida.setColor(Color.WHITE);
        fonteVida.getData().setScale(2f);

        fontePontuacao = new BitmapFont();
        fontePontuacao.setColor(Color.YELLOW);
        fontePontuacao.getData().setScale(2f);
    }

    public void drawHUD(SpriteBatch batch, ShapeRenderer shapeRenderer,
                        Viewport viewport, OrthographicCamera camera,
                        int vida, int vidaMax, int pontuacao) {

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barX = camera.position.x - viewport.getWorldWidth() / 2 + 30;
        float barY = camera.position.y + viewport.getWorldHeight() / 2 - 40;
        float barWidth = 200;
        float barHeight = 20;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float vidaPercentual = (float) vida / vidaMax;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth * vidaPercentual, barHeight);

        shapeRenderer.end();

        batch.begin();
        fonteVida.draw(batch, "Vida: " + vida + "/" + vidaMax, barX, barY + 40);
        fontePontuacao.draw(batch, "Queijos: " + pontuacao, barX, barY - 10);
        batch.end();
    }

    public void dispose() {
        fonteVida.dispose();
        fontePontuacao.dispose();
    }
}

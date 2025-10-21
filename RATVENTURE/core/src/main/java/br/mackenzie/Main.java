package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    private Texture backgroundTexture;
    private Texture ratoAndar1Texture;
    private FitViewport viewport;
    private SpriteBatch spriteBatch;
    private OrthographicCamera camera;
    private Sprite ratoAndar1Sprite;

    @Override
    public void create() {
        // Cria o batch
        spriteBatch = new SpriteBatch();

        // Carrega as texturas
        backgroundTexture = new Texture("fundoesgoto.png");
        ratoAndar1Texture = new Texture("ratoAndar1.png");

        // Cria a câmera e o viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(895, 540, camera);
        camera.position.set(447, 270, 0);
        camera.update();

        // Cria o sprite do rato
        ratoAndar1Sprite = new Sprite(ratoAndar1Texture);
        ratoAndar1Sprite.setPosition(200, 100); // posição inicial do rato
        ratoAndar1Sprite.setSize(100, 80);      // tamanho ajustável se quiser
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height);
    }

    @Override
    public void render() {
        // Atualiza lógica (se precisar)
        logic();
        input();

        // Desenha
        draw();
    }

    private void input() {
        // Aqui depois você pode colocar movimentação do rato, ex:
        // if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) ratoAndar1Sprite.translateX(2);
    }

    private void logic() {
        // Lógica do jogo (por enquanto vazia)
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();

        // Fundo
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();
        spriteBatch.draw(backgroundTexture, 0, 0, width, height);

        // Rato
        ratoAndar1Sprite.draw(spriteBatch);

        spriteBatch.end();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        spriteBatch.dispose();
        backgroundTexture.dispose();
        ratoAndar1Texture.dispose();
    }
}

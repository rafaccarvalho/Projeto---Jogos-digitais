package br.mackenzie.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import br.mackenzie.Main;
import com.badlogic.gdx.utils.viewport.Viewport;

public class StartScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private Texture telaInicial;
    private OrthographicCamera camera;
    private Viewport viewport;

    // Tamanho da tela
    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    public StartScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        telaInicial = new Texture("telainicial.png");

        camera = new OrthographicCamera();

        viewport = new com.badlogic.gdx.utils.viewport.StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(telaInicial, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            game.setScreen(new GameScreen1(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        telaInicial.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

package br.mackenzie.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import br.mackenzie.Main;
import br.mackenzie.entities.*;
import br.mackenzie.ui.HUD;

public class GameScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    // Fundo
    private Texture background;
    private float backgroundWidth;
    private final float worldWidth = 3000;

    // Entidades
    private Player player;
    private Cheese cheese;
    private RottenCheese rottenCheese;
    private Trap trap;

    // HUD e variáveis de jogo
    private HUD hud;
    private int pontuacao = 0;
    private int vidaRato = 100;
    private final int VIDA_MAX = 100;
    private final int VIDA_POR_QUEIJO = 20;
    private final int DANO_RATOEIRA = 10;
    private final int DANO_QUEIJO_ESTRAGADO = 10;

    private boolean gameOver = false;
    private Texture gameOverTexture;
    private float gameOverTimer = 0;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(895, 540, camera);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        background = new Texture("fundoesgoto.png");
        backgroundWidth = background.getWidth();

        player = new Player();
        cheese = new Cheese();
        rottenCheese = new RottenCheese();
        trap = new Trap();

        hud = new HUD();
        gameOverTexture = new Texture("gameover.png");

        updateCamera();
    }

    @Override
    public void render(float delta) {
        if (!gameOver) {
            handleInput();
            logic(delta);
        } else {
            gameOverTimer -= delta;
            if (gameOverTimer <= 0 && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                resetGame();
            }
        }

        draw();
    }

    private void handleInput() {
        player.handleInput();
    }

    private void logic(float delta) {
        player.updateJump();

        if (cheese.checkCollision(player.getSprite())) {
            pontuacao++;
            vidaRato = Math.min(VIDA_MAX, vidaRato + VIDA_POR_QUEIJO);
        }

        if (rottenCheese.checkCollision(player.getSprite())) {
            vidaRato -= DANO_QUEIJO_ESTRAGADO;
        }

        if (trap.checkCollision(player.getSprite())) {
            vidaRato -= DANO_RATOEIRA;
        }

        if (vidaRato <= 0) {
            gameOver = true;
            gameOverTimer = 1.5f;
        }

        updateCamera();
    }

    private void updateCamera() {
        float targetX = player.getCenterX();
        float cameraHalfWidth = viewport.getWorldWidth() / 2;
        targetX = MathUtils.clamp(targetX, cameraHalfWidth, worldWidth - cameraHalfWidth);
        camera.position.set(targetX, 270, 0);
        camera.update();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float height = viewport.getWorldHeight();
        int numBackgrounds = (int) Math.ceil(worldWidth / backgroundWidth) + 1;
        for (int i = 0; i < numBackgrounds; i++) {
            batch.draw(background, i * backgroundWidth, 0, backgroundWidth, height);
        }

        if (!gameOver) {
            cheese.draw(batch);
            rottenCheese.draw(batch);
            trap.draw(batch);
            player.draw(batch);
        } else {
            batch.draw(gameOverTexture, camera.position.x - viewport.getWorldWidth() / 2,
                camera.position.y - viewport.getWorldHeight() / 2,
                viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        batch.end();

        if (!gameOver)
            hud.drawHUD(batch, shapeRenderer, viewport, camera, vidaRato, VIDA_MAX, pontuacao);
    }

    private void resetGame() {
        vidaRato = VIDA_MAX;
        pontuacao = 0;
        player.reset();
        cheese.respawn();
        rottenCheese.respawn();
        trap.respawn();
        gameOver = false;
    }

    @Override
    public void dispose() {
        batch.dispose();
        background.dispose();
        shapeRenderer.dispose();
        gameOverTexture.dispose();
        player.dispose();
        cheese.dispose();
        rottenCheese.dispose();
        trap.dispose();
        hud.dispose();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

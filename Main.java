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
    private boolean isJumping = false;
    private float jumpSpeed = 8;
    private float gravity = -0.4f;
    private float velocityY = 0;
    private float groundY = 100;
    
    // variáveis para fundo infinito
    private float backgroundWidth;
    private float worldWidth = 3000; // Mundo 3x maior que a tela
    private float cameraOffsetX = 447; // Posição inicial da câmera

    @Override
    public void create() {
        // cria o batch
        spriteBatch = new SpriteBatch();

        // carrega as text
        backgroundTexture = new Texture("fundoesgoto.png");
        ratoAndar1Texture = new Texture("ratoAndar1.png");

        // cria a câmera e o viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(895, 540, camera);
        
        // Cria o sprite do rato
        ratoAndar1Sprite = new Sprite(ratoAndar1Texture);
        ratoAndar1Sprite.setPosition(200, groundY);
        ratoAndar1Sprite.setSize(100, 80);
        
        // Configura fundo infinito
        backgroundWidth = backgroundTexture.getWidth();
        
        // Posiciona câmera inicial
        updateCamera();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height);
    }

    @Override
    public void render() {
        // Atualiza lógica
        input();
        logic();
        updateCamera();

        // Desenha
        draw();
    }

    private void input() {
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            ratoAndar1Sprite.translateX(2);
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            ratoAndar1Sprite.translateX(-2);
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE) && !isJumping) {
            isJumping = true;
            velocityY = jumpSpeed;
        }
    }

    private void logic() {
        if (isJumping) {
            ratoAndar1Sprite.translateY(velocityY);
            velocityY += gravity;

            if (ratoAndar1Sprite.getY() <= groundY) {
                ratoAndar1Sprite.setY(groundY);
                isJumping = false;
                velocityY = 0;
            }
        }
        
        // Limites do mundo
        if (ratoAndar1Sprite.getX() < 0) {
            ratoAndar1Sprite.setX(0);
        }
        if (ratoAndar1Sprite.getX() > worldWidth - ratoAndar1Sprite.getWidth()) {
            ratoAndar1Sprite.setX(worldWidth - ratoAndar1Sprite.getWidth());
        }
    }
    
    private void updateCamera() {
        // Faz a câmera seguir o rato, mas sem ultrapassar os limites do mundo
        float targetX = ratoAndar1Sprite.getX() + ratoAndar1Sprite.getWidth() / 2;
        
        // Limita a câmera para não mostrar áreas fora do mundo
        float cameraHalfWidth = viewport.getWorldWidth() / 2;
        if (targetX < cameraHalfWidth) {
            targetX = cameraHalfWidth;
        }
        if (targetX > worldWidth - cameraHalfWidth) {
            targetX = worldWidth - cameraHalfWidth;
        }
        
        camera.position.set(targetX, 270, 0);
        camera.update();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();

        // Desenha fundo infinito - repete a textura várias vezes
        float height = viewport.getWorldHeight();
        int numBackgrounds = (int) Math.ceil(worldWidth / backgroundWidth) + 1;
        
        for (int i = 0; i < numBackgrounds; i++) {
            spriteBatch.draw(backgroundTexture, i * backgroundWidth, 0, backgroundWidth, height);
        }

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
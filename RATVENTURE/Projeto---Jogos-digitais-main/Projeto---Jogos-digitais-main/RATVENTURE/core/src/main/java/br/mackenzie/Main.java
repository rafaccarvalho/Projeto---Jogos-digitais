package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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
    private final float jumpSpeed = 8;
    private final float gravity = -0.4f;
    private float velocityY = 0;
    private final float groundY = 100;

    // Fundo infinito
    private float backgroundWidth;
    private final float worldWidth = 3000;

    // POWER-UP - queijo
    private Texture queijoTexture;
    private Sprite queijoSprite;
    private boolean queijoAtivo = true;
    private float queijoRespawnTimer = 0;
    private final float queijoRespawnDelay = 3f; // reaparece após 3 segundos

    // Vida do rato
    private int vidaRato = 100;
    private final int VIDA_MAX = 100;
    private final int VIDA_POR_QUEIJO = 20; // quanto o queijo recupera

    // ---- NOVO: pontuação ----
    private int pontuacao = 0;
    private BitmapFont fontePontuacao;

    // Visualização das hitboxes
    private ShapeRenderer shapeRenderer;
    private final boolean mostrarColisoes = false;

    // Interface de vida
    private BitmapFont fonteVida;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();

        backgroundTexture = new Texture("fundoesgoto.png");
        ratoAndar1Texture = new Texture("ratoAndar1.png");
        queijoTexture = new Texture("queijo.png");

        camera = new OrthographicCamera();
        viewport = new FitViewport(895, 540, camera);

        // Rato
        ratoAndar1Sprite = new Sprite(ratoAndar1Texture);
        ratoAndar1Sprite.setPosition(200, groundY);
        ratoAndar1Sprite.setSize(100, 80);

        // Fundo
        backgroundWidth = backgroundTexture.getWidth();

        // Queijo
        queijoSprite = new Sprite(queijoTexture);
        queijoSprite.setSize(250, 250);
        gerarPosicaoQueijo();

        // HUD
        shapeRenderer = new ShapeRenderer();
        fonteVida = new BitmapFont();
        fonteVida.setColor(Color.WHITE);
        fonteVida.getData().setScale(2f);

        // ---- NOVO: fonte da pontuação ----
        fontePontuacao = new BitmapFont();
        fontePontuacao.setColor(Color.YELLOW);
        fontePontuacao.getData().setScale(2f);

        updateCamera();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height);
    }

    @Override
    public void render() {
        if (vidaRato > 0) {
            input();
            logic();
            updateCamera();
        }
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
        // Movimento do pulo
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
        if (ratoAndar1Sprite.getX() < 0)
            ratoAndar1Sprite.setX(0);
        if (ratoAndar1Sprite.getX() > worldWidth - ratoAndar1Sprite.getWidth())
            ratoAndar1Sprite.setX(worldWidth - ratoAndar1Sprite.getWidth());

        // Lógica do queijo
        if (queijoAtivo) {
            if (colideComHitboxReduzida(ratoAndar1Sprite, queijoSprite)) {
                queijoAtivo = false;
                queijoRespawnTimer = queijoRespawnDelay;
                vidaRato = Math.min(VIDA_MAX, vidaRato + VIDA_POR_QUEIJO);

                // ---- NOVO: soma pontuação ao pegar queijo ----
                pontuacao++;
            }
        } else {
            queijoRespawnTimer -= Gdx.graphics.getDeltaTime();
            if (queijoRespawnTimer <= 0) {
                queijoAtivo = true;
                gerarPosicaoQueijo();
            }
        }

        if (vidaRato <= 0) {
            vidaRato = 0;
        }
    }

    private boolean colideComHitboxReduzida(Sprite rato, Sprite queijo) {
        Rectangle r = rato.getBoundingRectangle();
        Rectangle q = queijo.getBoundingRectangle();

        final float RATO_HITBOX_SCALE_X = 0.8f;
        final float RATO_HITBOX_SCALE_Y = 0.7f;
        final float QUEIJO_HITBOX_SCALE_X = 0.35f;
        final float QUEIJO_HITBOX_SCALE_Y = 0.35f;

        float rHitW = r.width * RATO_HITBOX_SCALE_X;
        float rHitH = r.height * RATO_HITBOX_SCALE_Y;
        float rHitX = r.x + (r.width - rHitW) / 2f;
        float rHitY = r.y + (r.height - rHitH) / 2f;
        Rectangle rHit = new Rectangle(rHitX, rHitY, rHitW, rHitH);

        float qHitW = q.width * QUEIJO_HITBOX_SCALE_X;
        float qHitH = q.height * QUEIJO_HITBOX_SCALE_Y;
        float qHitX = q.x + (q.width - qHitW) / 2f;
        float qHitY = q.y + (q.height - qHitH) / 2f;
        Rectangle qHit = new Rectangle(qHitX, qHitY, qHitW, qHitH);

        return rHit.overlaps(qHit);
    }

    private void gerarPosicaoQueijo() {
        float margin = 50;
        float x = MathUtils.random(margin, worldWidth - queijoSprite.getWidth() - margin);
        float y = groundY + 20;
        queijoSprite.setPosition(x, y);
    }

    private void updateCamera() {
        float targetX = ratoAndar1Sprite.getX() + ratoAndar1Sprite.getWidth() / 2;
        float cameraHalfWidth = viewport.getWorldWidth() / 2;

        if (targetX < cameraHalfWidth)
            targetX = cameraHalfWidth;
        if (targetX > worldWidth - cameraHalfWidth)
            targetX = worldWidth - cameraHalfWidth;

        camera.position.set(targetX, 270, 0);
        camera.update();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();

        // Fundo infinito
        float height = viewport.getWorldHeight();
        int numBackgrounds = (int) Math.ceil(worldWidth / backgroundWidth) + 1;
        for (int i = 0; i < numBackgrounds; i++) {
            spriteBatch.draw(backgroundTexture, i * backgroundWidth, 0, backgroundWidth, height);
        }

        // Queijo
        if (queijoAtivo)
            queijoSprite.draw(spriteBatch);

        // Rato
        if (vidaRato > 0)
            ratoAndar1Sprite.draw(spriteBatch);

        spriteBatch.end();

        // HUDs
        drawVidaHUD();
        drawPontuacaoHUD();
    }

    private void drawVidaHUD() {
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float barX = camera.position.x - viewport.getWorldWidth() / 2 + 30;
        float barY = camera.position.y + viewport.getWorldHeight() / 2 - 40;
        float barWidth = 200;
        float barHeight = 20;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float vidaPercentual = (float) vidaRato / VIDA_MAX;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth * vidaPercentual, barHeight);

        shapeRenderer.end();

        spriteBatch.begin();
        fonteVida.draw(spriteBatch, "Vida: " + vidaRato + "/" + VIDA_MAX, barX, barY + 40);
        spriteBatch.end();
    }

    // ---- NOVO: HUD da pontuação ----
    private void drawPontuacaoHUD() {
        spriteBatch.begin();
        float textX = camera.position.x - viewport.getWorldWidth() / 2 + 30;
        float textY = camera.position.y + viewport.getWorldHeight() / 2 - 90; // abaixo da vida
        fontePontuacao.draw(spriteBatch, "Queijos: " + pontuacao, textX, textY);
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
        queijoTexture.dispose();
        shapeRenderer.dispose();
        fonteVida.dispose();
        fontePontuacao.dispose();
    }
}

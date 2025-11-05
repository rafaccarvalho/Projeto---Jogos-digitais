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
    private final float jumpSpeed = 6;
    private final float gravity = -0.10f;
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
    private final float queijoRespawnDelay = 3f;

    // Obstáculo - ratoeira
    private Texture ratoeiraTexture;
    private Sprite ratoeiraSprite;

    // NOVO OBSTÁCULO - queijo estragado
    private Texture queijoEstragadoTexture;
    private Sprite queijoEstragadoSprite;
    private boolean queijoEstragadoAtivo = true;
    private float queijoEstragadoRespawnTimer = 0;
    private final float queijoEstragadoRespawnDelay = 4f;
    private final int DANO_QUEIJO_ESTRAGADO = 10;

    // Vida do rato
    private int vidaRato = 100;
    private final int VIDA_MAX = 100;
    private final int VIDA_POR_QUEIJO = 20;
    private final int DANO_RATOEIRA = 10;

    // ---- pontuação ----
    private int pontuacao = 0;
    private BitmapFont fontePontuacao;

    // Visualização das hitboxes
    private ShapeRenderer shapeRenderer;

    // Interface de vida
    private BitmapFont fonteVida;

    // ---- Game Over ----
    private Texture gameOverTexture;
    private boolean gameOver = false;
    private float gameOverTimer = 0f; // tempo até permitir reiniciar

    // ---- Tela Inicial ----
    private Texture telaInicialTexture;
    private boolean jogoIniciado = false;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();

        backgroundTexture = new Texture("fundoesgoto.png");
        ratoAndar1Texture = new Texture("ratoAndar1.png");
        queijoTexture = new Texture("queijo.png");
        ratoeiraTexture = new Texture("ratoeira.png");
        gameOverTexture = new Texture("gameover.png");
        telaInicialTexture = new Texture("telainicial.png");
        queijoEstragadoTexture = new Texture("queijoestragado.png"); // novo obstáculo

        camera = new OrthographicCamera();
        viewport = new FitViewport(895, 540, camera);

        ratoAndar1Sprite = new Sprite(ratoAndar1Texture);
        ratoAndar1Sprite.setPosition(200, groundY);
        ratoAndar1Sprite.setSize(100, 55);

        backgroundWidth = backgroundTexture.getWidth();

        queijoSprite = new Sprite(queijoTexture);
        queijoSprite.setSize(50, 40);
        gerarPosicaoQueijo();

        ratoeiraSprite = new Sprite(ratoeiraTexture);
        ratoeiraSprite.setSize(120, 60);
        gerarPosicaoRatoeira();

        queijoEstragadoSprite = new Sprite(queijoEstragadoTexture);
        queijoEstragadoSprite.setSize(50, 40);
        gerarPosicaoQueijoEstragado();

        shapeRenderer = new ShapeRenderer();
        fonteVida = new BitmapFont();
        fonteVida.setColor(Color.WHITE);
        fonteVida.getData().setScale(2f);

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
        // ----- TELA INICIAL -----
        if (!jogoIniciado) {
            ScreenUtils.clear(Color.BLACK);
            viewport.apply();
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();

            float screenWidth = viewport.getWorldWidth();
            float screenHeight = viewport.getWorldHeight();
            float imageWidth = telaInicialTexture.getWidth();
            float imageHeight = telaInicialTexture.getHeight();
            float scale = Math.min(screenWidth / imageWidth, screenHeight / imageHeight);
            float drawWidth = imageWidth * scale;
            float drawHeight = imageHeight * scale;
            float drawX = (screenWidth - drawWidth) / 2f;
            float drawY = (screenHeight - drawHeight) / 2f;
            spriteBatch.draw(telaInicialTexture, drawX, drawY, drawWidth, drawHeight);
            spriteBatch.end();

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                jogoIniciado = true;
            }
            return;
        }

        // ----- JOGO NORMAL -----
        if (!gameOver) {
            if (vidaRato > 0) {
                input();
                logic();
                updateCamera();
            } else {
                gameOver = true;
                gameOverTimer = 1.5f;
            }
        } else {
            gameOverTimer -= Gdx.graphics.getDeltaTime();
            if (gameOverTimer <= 0 && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
                reiniciarJogo();
            }
        }

        draw();
    }

    private void input() {
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            ratoAndar1Sprite.translateX(2);
            if (ratoAndar1Sprite.isFlipX()) {
                ratoAndar1Sprite.flip(true, false);
            }
        }

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            ratoAndar1Sprite.translateX(-2);
            if (!ratoAndar1Sprite.isFlipX()) {
                ratoAndar1Sprite.flip(true, false);
            }
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

        if (ratoAndar1Sprite.getX() < 0)
            ratoAndar1Sprite.setX(0);
        if (ratoAndar1Sprite.getX() > worldWidth - ratoAndar1Sprite.getWidth())
            ratoAndar1Sprite.setX(worldWidth - ratoAndar1Sprite.getWidth());

        if (queijoAtivo) {
            if (colideComHitboxReduzida(ratoAndar1Sprite, queijoSprite)) {
                queijoAtivo = false;
                queijoRespawnTimer = queijoRespawnDelay;
                vidaRato = Math.min(VIDA_MAX, vidaRato + VIDA_POR_QUEIJO);
                pontuacao++;
            }
        } else {
            queijoRespawnTimer -= Gdx.graphics.getDeltaTime();
            if (queijoRespawnTimer <= 0) {
                queijoAtivo = true;
                gerarPosicaoQueijo();
            }
        }

        // lógica queijo estragado
        if (queijoEstragadoAtivo) {
            if (colideComHitboxReduzida(ratoAndar1Sprite, queijoEstragadoSprite)) {
                queijoEstragadoAtivo = false;
                queijoEstragadoRespawnTimer = queijoEstragadoRespawnDelay;
                vidaRato -= DANO_QUEIJO_ESTRAGADO;
                if (vidaRato < 0) vidaRato = 0;
            }
        } else {
            queijoEstragadoRespawnTimer -= Gdx.graphics.getDeltaTime();
            if (queijoEstragadoRespawnTimer <= 0) {
                queijoEstragadoAtivo = true;
                gerarPosicaoQueijoEstragado();
            }
        }

        if (colideComHitboxReduzida(ratoAndar1Sprite, ratoeiraSprite)) {
            if (!isJumping) {
                vidaRato -= DANO_RATOEIRA;
                if (vidaRato < 0) vidaRato = 0;
                gerarPosicaoRatoeira();
            }
        }
    }

    private boolean colideComHitboxReduzida(Sprite a, Sprite b) {
        Rectangle r1 = a.getBoundingRectangle();
        Rectangle r2 = b.getBoundingRectangle();
        float scaleX = 0.8f;
        float scaleY = 0.7f;
        Rectangle r1hit = new Rectangle(
            r1.x + (r1.width * (1 - scaleX) / 2f),
            r1.y + (r1.height * (1 - scaleY) / 2f),
            r1.width * scaleX,
            r1.height * scaleY
        );
        Rectangle r2hit = new Rectangle(
            r2.x + (r2.width * 0.1f),
            r2.y + (r2.height * 0.1f),
            r2.width * 0.8f,
            r2.height * 0.8f
        );
        return r1hit.overlaps(r2hit);
    }

    private void gerarPosicaoQueijo() {
        float margin = 50;
        float x = MathUtils.random(margin, worldWidth - queijoSprite.getWidth() - margin);
        float y = MathUtils.random(groundY + 80, groundY + 200);
        queijoSprite.setPosition(x, y);
    }

    private void gerarPosicaoQueijoEstragado() {
        float margin = 50;
        float x = MathUtils.random(margin, worldWidth - queijoEstragadoSprite.getWidth() - margin);
        float y = MathUtils.random(groundY + 80, groundY + 200);
        queijoEstragadoSprite.setPosition(x, y);
    }

    private void gerarPosicaoRatoeira() {
        float margin = 100;
        float x = MathUtils.random(margin, worldWidth - ratoeiraSprite.getWidth() - margin);
        float y = groundY;
        ratoeiraSprite.setPosition(x, y);
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

        float height = viewport.getWorldHeight();
        int numBackgrounds = (int) Math.ceil(worldWidth / backgroundWidth) + 1;
        for (int i = 0; i < numBackgrounds; i++) {
            spriteBatch.draw(backgroundTexture, i * backgroundWidth, 0, backgroundWidth, height);
        }

        if (!gameOver) {
            if (queijoAtivo)
                queijoSprite.draw(spriteBatch);
            if (queijoEstragadoAtivo)
                queijoEstragadoSprite.draw(spriteBatch);
            ratoeiraSprite.draw(spriteBatch);
            ratoAndar1Sprite.draw(spriteBatch);
        } else {
            spriteBatch.draw(gameOverTexture,
                camera.position.x - viewport.getWorldWidth() / 2,
                camera.position.y - viewport.getWorldHeight() / 2,
                viewport.getWorldWidth(), viewport.getWorldHeight());
        }

        spriteBatch.end();

        if (!gameOver) {
            drawVidaHUD();
            drawPontuacaoHUD();
        }
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

    private void drawPontuacaoHUD() {
        spriteBatch.begin();
        float textX = camera.position.x - viewport.getWorldWidth() / 2 + 30;
        float textY = camera.position.y + viewport.getWorldHeight() / 2 - 90;
        fontePontuacao.draw(spriteBatch, "Queijos: " + pontuacao, textX, textY);
        spriteBatch.end();
    }

    private void reiniciarJogo() {
        vidaRato = VIDA_MAX;
        pontuacao = 0;
        ratoAndar1Sprite.setPosition(200, groundY);
        gerarPosicaoQueijo();
        gerarPosicaoQueijoEstragado();
        gerarPosicaoRatoeira();
        gameOver = false;
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
        queijoEstragadoTexture.dispose();
        ratoeiraTexture.dispose();
        shapeRenderer.dispose();
        fonteVida.dispose();
        fontePontuacao.dispose();
        gameOverTexture.dispose();
        telaInicialTexture.dispose();
    }
}

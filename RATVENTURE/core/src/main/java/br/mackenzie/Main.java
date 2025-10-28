package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
    private final float cameraOffsetX = 447;

    //POWER-UP - queijo
    private Texture queijoTexture;
    private Sprite queijoSprite;
    private boolean queijoAtivo = true;
    private float queijoRespawnTimer = 0;
    private final float queijoRespawnDelay = 3f; // reaparece após 3 segundos

    // Visualização das hitboxes
    private ShapeRenderer shapeRenderer;
    private final boolean mostrarColisoes = true; // set false para desligar

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

        //Cria o queijo (visual grande)
        queijoSprite = new Sprite(queijoTexture);
        queijoSprite.setSize(250, 250); // tamanho visual desejado
        gerarPosicaoQueijo();

        // Ferramenta para desenhar retângulos de colisão
        shapeRenderer = new ShapeRenderer();

        updateCamera();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height);
    }

    @Override
    public void render() {
        input();
        logic();
        updateCamera();
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

        //Lógica do Power-up - queijo
        if (queijoAtivo) {
            // Verifica colisão com HITBOX reduzida
            if (colideComHitboxReduzida(ratoAndar1Sprite, queijoSprite)) {
                queijoAtivo = false;
                queijoRespawnTimer = queijoRespawnDelay;
            }
        } else {
            // Contagem regressiva até reaparecer
            queijoRespawnTimer -= Gdx.graphics.getDeltaTime();
            if (queijoRespawnTimer <= 0) {
                queijoAtivo = true;
                gerarPosicaoQueijo();
            }
        }
    }

    private boolean colideComHitboxReduzida(Sprite rato, Sprite queijo) {
        Rectangle r = rato.getBoundingRectangle();   // usa posicionamento atual do sprite
        Rectangle q = queijo.getBoundingRectangle();

        // Fatores: quanto do sprite será considerado na hitbox (0.0 - 1.0)
        final float RATO_HITBOX_SCALE_X = 0.8f;  // 80% da largura do rato
        final float RATO_HITBOX_SCALE_Y = 0.7f;  // 70% da altura do rato
        final float QUEIJO_HITBOX_SCALE_X = 0.35f; // 35% da largura do queijo (menor)
        final float QUEIJO_HITBOX_SCALE_Y = 0.35f; // 35% da altura do queijo (menor)

        // Calcula hitbox do rato (centralizada)
        float rHitW = r.width * RATO_HITBOX_SCALE_X;
        float rHitH = r.height * RATO_HITBOX_SCALE_Y;
        float rHitX = r.x + (r.width - rHitW) / 2f;
        float rHitY = r.y + (r.height - rHitH) / 2f;
        Rectangle rHit = new Rectangle(rHitX, rHitY, rHitW, rHitH);

        // Calcula hitbox do queijo (centralizada e bem menor)
        float qHitW = q.width * QUEIJO_HITBOX_SCALE_X;
        float qHitH = q.height * QUEIJO_HITBOX_SCALE_Y;
        float qHitX = q.x + (q.width - qHitW) / 2f;
        float qHitY = q.y + (q.height - qHitH) / 2f;
        Rectangle qHit = new Rectangle(qHitX, qHitY, qHitW, qHitH);

        return rHit.overlaps(qHit);
    }

    private void gerarPosicaoQueijo() {
        // Gera nova posição dentro do mundo (evita colidir com borda)
        float margin = 50;
        float x = MathUtils.random(margin, worldWidth - queijoSprite.getWidth() - margin);
        float y = groundY + 20; // levemente acima do chão
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

        // Desenha queijo se ativo
        if (queijoAtivo)
            queijoSprite.draw(spriteBatch);

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
        queijoTexture.dispose();
        shapeRenderer.dispose();
    }
}

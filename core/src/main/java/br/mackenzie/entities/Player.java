package br.mackenzie.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {

    public static final float PPM = 90f;

    // --- Física e Controle ---
    private Body body;
    private Sprite sprite;
    private boolean isGrounded = false; // Essencial para o pulo
    private float currentPedalValue = 0f; // Valor normalizado (0.0 a 1.0) do PedalController

    // --- Constantes de Movimento e Jogo ---
    private final float JUMP_FORCE = 2f;      // Força de impulso vertical
    private final float MAX_SPEED_CAP = 5f;   // Velocidade horizontal máxima

    private int vida = 100;
    private final int VIDA_MAX = 100;

    public Player(World world, float x, float y) {
        createBody(world, x, y);

        // Troque o nome da textura pelo seu sprite
        Texture texture = new Texture("ratoAndar1.png");
        sprite = new Sprite(texture);
        sprite.setSize(32 / PPM, 32 / PPM);
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
    }

    private void createBody(World world, float x, float y) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(x / PPM, y / PPM);

        body = world.createBody(bdef);
        body.setFixedRotation(true); // Impede que o rato role

        // 1. Fixture Principal (Corpo do Rato)
        PolygonShape mainShape = new PolygonShape();
        // Tamanho ajustado (14px de largura, 16px de altura)
        mainShape.setAsBox(10 / PPM, 10 / PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = mainShape;
        fdef.density = 8f;
        fdef.friction = 0.2f;

        // UserData "player" para colisões com coletáveis
        body.createFixture(fdef).setUserData("player");
        mainShape.dispose();

        // 2. Fixture do Sensor de Pé (para detectar o chão)
        PolygonShape footShape = new PolygonShape();
        // Posição: no centro (0, 0) do corpo, mas offset para baixo (-16/PPM)
        footShape.setAsBox(12 / PPM, 2 / PPM, new Vector2(0, -16 / PPM), 0);

        fdef.shape = footShape;
        fdef.isSensor = true; // Sensor não causa colisão física
        fdef.friction = 0f;

        // UserData "foot" para detectar o chão no ContactListener
        body.createFixture(fdef).setUserData("foot");
        footShape.dispose();
    }

    /**
     * Recebe o valor normalizado (0.0 a 1.0) do PedalController.
     */
    public void setPedalVelocity(float normalizedValue) {
        this.currentPedalValue = normalizedValue;
    }

    /**
     * Processa entradas de teclado para ações como PULO.
     * Chamado antes de world.step().
     */
    // NO MÓDULO CORE: Player.java

// ...

// Constante de velocidade lateral para o teclado (apenas para fallback, se necessário)
    private final float KEYBOARD_SIDEL_SPEED = 0.2f;


    public void handleInput() {

        // 1. Lógica de Pulo (SPACE ou UP)
        // Mantém a lógica de pulo existente (aplicação de impulso vertical)
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.UP)) && isGrounded()) {

            body.applyLinearImpulse(
                new Vector2(0, JUMP_FORCE),
                body.getWorldCenter(),
                true
            );
        }

        // 2. Lógica de Movimento Horizontal (Direção)
        // Se o teclado for usado para movimento lateral, sobrepõe a velocidade.
        Vector2 vel = body.getLinearVelocity();
        float targetXVelocity = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            // Aplica uma velocidade para a esquerda
            targetXVelocity = -MAX_SPEED_CAP * KEYBOARD_SIDEL_SPEED;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            // Aplica uma velocidade para a direita
            targetXVelocity = MAX_SPEED_CAP * KEYBOARD_SIDEL_SPEED;
        }

        // Se uma tecla de direção for pressionada, define a velocidade horizontal
        if (targetXVelocity != 0) {
            body.setLinearVelocity(targetXVelocity, vel.y);
        }
        // Se nenhuma tecla de direção for pressionada, o movimento lateral é ZERO,
        // mas a aceleração do pedal (PedalController) deve assumir.
    }

    public void update(float delta) {
        // 1. Movimento Horizontal (Baseado no valor do pedal)
        float targetSpeed = currentPedalValue * MAX_SPEED_CAP;
        Vector2 velocity = body.getLinearVelocity();

        // Aplica velocidade, mantendo a velocidade vertical intacta
        // A suavização (0.9 e 0.1) evita aceleração instantânea
        float newXVelocity = (velocity.x * 0.9f) + (targetSpeed * 0.1f);

        body.setLinearVelocity(newXVelocity, velocity.y);

        // 2. Atualiza sprite
        sprite.setPosition(
            body.getPosition().x - sprite.getWidth() / 2,
            body.getPosition().y - sprite.getHeight() / 2
        );

        // (Opcional: Lógica de rotação ou flip do sprite para a direção do movimento)
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    // --- Getters e Setters para a BaseGameScreen e ContactListener ---

    public Body getBody() {
        return body;
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public int getVida() {
        return vida;
    }

    public int getVidaMax() {
        return VIDA_MAX;
    }

    public void setVida(int vida) {
        // Garante que a vida não exceda o máximo
        this.vida = Math.min(vida, VIDA_MAX);
    }
}

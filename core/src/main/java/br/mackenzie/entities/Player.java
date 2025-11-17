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


    private Body body;
    private Sprite sprite;
    private boolean isGrounded = false; // Essencial para o pulo
    private float currentPedalValue = 0f; // Valor normalizado (0.0 a 1.0) do PedalController


    private final float JUMP_FORCE = 2f;      // Força de impulso vertical
    private final float MAX_SPEED_CAP = 5f;   // Velocidade horizontal máxima

    private final int vidaAtual = 100;
    private final int vidaMax = 100;

    public Player(World world, float x, float y) {
        createBody(world, x, y);


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
        body.setFixedRotation(true);


        PolygonShape mainShape = new PolygonShape();
        mainShape.setAsBox(10 / PPM, 10 / PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = mainShape;
        fdef.density = 8f;
        fdef.friction = 0.2f;

        body.createFixture(fdef).setUserData("player");
        mainShape.dispose();


        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox(12 / PPM, 2 / PPM, new Vector2(0, -16 / PPM), 0);

        fdef.shape = footShape;
        fdef.isSensor = true;
        fdef.friction = 0f;

        body.createFixture(fdef).setUserData("foot");
        footShape.dispose();
    }

    public void setPedalVelocity(float normalizedValue) {
        this.currentPedalValue = normalizedValue;
    }

    private final float KEYBOARD_SIDEL_SPEED = 0.2f;

    public void handleInput() {

        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.UP)) && isGrounded()) {

            body.applyLinearImpulse(
                new Vector2(0, JUMP_FORCE),
                body.getWorldCenter(),
                true
            );
        }

        Vector2 vel = body.getLinearVelocity();
        float targetXVelocity = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            targetXVelocity = -MAX_SPEED_CAP * KEYBOARD_SIDEL_SPEED;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            targetXVelocity = MAX_SPEED_CAP * KEYBOARD_SIDEL_SPEED;
        }

        if (targetXVelocity != 0) {
            body.setLinearVelocity(targetXVelocity, vel.y);
        }
    }

    public void update(float delta) {
        float targetSpeed = currentPedalValue * MAX_SPEED_CAP;
        Vector2 velocity = body.getLinearVelocity();

        float newXVelocity = (velocity.x * 0.9f) + (targetSpeed * 0.1f);

        body.setLinearVelocity(newXVelocity, velocity.y);

        sprite.setPosition(
            body.getPosition().x - sprite.getWidth() / 2,
            body.getPosition().y - sprite.getHeight() / 2
        );
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public Body getBody() {
        return body;
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public int getVidaAtual(){
        return vidaAtual;
    }

    public int getVidaMax(){
        return vidaMax;
    }
}

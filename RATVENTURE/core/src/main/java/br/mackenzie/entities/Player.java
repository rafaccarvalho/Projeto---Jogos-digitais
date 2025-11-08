package br.mackenzie.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Player {
    public static final float PPM = 15f;

    private World world;
    private Body body;
    private Sprite sprite;

    private Texture texIdle , texRight , textLeft;

    private boolean isGrounded = false;
    private boolean facingRight = true;


    private final float moveSpeed = 55f;
    private final float jumpImpulse = 10f;
    private final float maxWalkVel = 4f;

    public Player(World world, float startXpx, float startYpx) {
        this.world = world;

        texIdle = new Texture("ratoFrente3.PNG");
        texRight = new Texture("ratoAndar1.PNG");
        textLeft = new Texture("ratoAndar2.PNG");


        // Sprite inicial (parado)
        sprite = new Sprite(texIdle);
        sprite.setSize(15f / PPM, 15f / PPM);

        defineBody(startXpx, startYpx);
    }

    private void defineBody(float startXpx, float startYpx) {
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(startXpx / PPM, startYpx / PPM);
        bdef.fixedRotation = true;
        body = world.createBody(bdef);

        PolygonShape bodyShape = new PolygonShape();
        bodyShape.setAsBox(sprite.getWidth() / 2f, sprite.getHeight() / 2f);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = bodyShape;
        fdef.density = 1f;
        fdef.friction = 0.4f;
        fdef.restitution = 0f;
        body.createFixture(fdef).setUserData("player");
        bodyShape.dispose();

        // Sensor do pé
        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox((sprite.getWidth() / 2f) * 0.9f, 5f / PPM,
            new Vector2(0, -(sprite.getHeight() / 2f)), 0);
        fdef.shape = footShape;
        fdef.isSensor = true;
        fdef.density = 0;
        body.createFixture(fdef).setUserData("foot");
        footShape.dispose();
    }

    public void handleInput() {
        Vector2 vel = body.getLinearVelocity();
        float desiredVelX = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            desiredVelX = moveSpeed;
            sprite.setTexture(texRight);
            facingRight = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            desiredVelX = -moveSpeed;
            sprite.setTexture(textLeft);
            facingRight = false;
        }else{
            sprite.setTexture(texIdle);
        }

        float velChangeX = desiredVelX - vel.x;
        float impulseX = body.getMass() * velChangeX;
        body.applyLinearImpulse(new Vector2(impulseX, 0), body.getWorldCenter(), true);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isGrounded) {
            body.applyLinearImpulse(new Vector2(0, jumpImpulse), body.getWorldCenter(), true);
            isGrounded = false;
        }

        if (Math.abs(body.getLinearVelocity().x) > maxWalkVel) {
            body.setLinearVelocity(Math.signum(body.getLinearVelocity().x) * maxWalkVel, body.getLinearVelocity().y);
        }
    }

    // ✅ Atualiza posição e animação
    public void update(float delta) {
        //sprite.setRegion((delta));

        sprite.setSize(40f / PPM, 25f / PPM);
        sprite.setPosition(
            body.getPosition().x - sprite.getWidth() / 2f,
            body.getPosition().y - sprite.getHeight() / 2f
        );

    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public Body getBody() { return body; }
    public void setGrounded(boolean g) { this.isGrounded = g; }

    public void resetPosition(float xpx, float ypx) {
        body.setTransform(xpx / PPM, ypx / PPM, 0);
        body.setLinearVelocity(0, 0);
        isGrounded = false;
    }

    public void dispose() {

    }
}

package br.mackenzie.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    // 🚨 PPM é a referência universal (20f)
    public static final float PPM = 20f;

    private World world;
    private Body body;
    private Texture texture;
    private Sprite sprite;

    private boolean isGrounded = false;

    private final float moveSpeed = 55f;
    private final float jumpImpulse = 25f;
    private final float maxWalkVel = 4f;

    public Player(World world, float startXpx, float startYpx) {
        this.world = world;
        texture = new Texture("ratoAndar1.png");
        sprite = new Sprite(texture);
        sprite.setSize(40f / PPM, 25f / PPM);
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

        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox(
            (sprite.getWidth() / 2f) * 0.9f,
            5f / PPM,
            new Vector2(0, -(sprite.getHeight() / 2f)),
            0
        );

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
            if (sprite.isFlipX()) sprite.flip(true, false);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            desiredVelX = -moveSpeed;
            if (!sprite.isFlipX()) sprite.flip(true, false);
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

    public void update() {
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
    public boolean isGrounded() { return this.isGrounded; }

    public void resetPosition(float xpx, float ypx) {
        body.setTransform(xpx / PPM, ypx / PPM, 0);
        body.setLinearVelocity(0, 0);
        isGrounded = false;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }

    public Sprite getSprite() {
        return sprite;
    }
}

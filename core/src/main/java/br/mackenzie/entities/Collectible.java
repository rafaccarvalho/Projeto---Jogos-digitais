package br.mackenzie.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

public abstract class Collectible {

    public static final float PPM = Player.PPM;

    protected World world;
    public Body body;
    protected Texture texture;
    protected Sprite sprite;

    public boolean ativo = true;
    public boolean markedForRemoval = false;

    public Collectible(World world, String texturePath, float spriteWidthPx, float spriteHeightPx, float xpx, float ypx) {
        this.world = world;
        this.texture = new Texture(texturePath);
        this.sprite = new Sprite(this.texture);
        this.sprite.setSize(spriteWidthPx / PPM, spriteHeightPx / PPM);

        createBody(xpx, ypx, this);
    }


    protected abstract Shape getShape(float widthPx, float heightPx);

    protected void createBody(float xpx, float ypx, Object userData) {

        if (body != null) {
        }

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(xpx / PPM, ypx / PPM);
        body = world.createBody(bdef);


        Shape shape = getShape(sprite.getWidth() * PPM, sprite.getHeight() * PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.isSensor = true;

        body.createFixture(fdef).setUserData(userData);
        shape.dispose();

        ativo = true;
        markedForRemoval = false;
    }


    public void recreateBody(float xpx, float ypx) {
        // Destrói o corpo Box2D antigo antes de recriar
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
        createBody(xpx, ypx, this);
    }

    public abstract void respawnRandom();


    public Body getBody() { return body; }


    public void update() {
        if (ativo && body != null) {
            // Sincroniza Sprite (Pixels) com Body (Metros)
            sprite.setPosition(
                body.getPosition().x - sprite.getWidth() / 2f,
                body.getPosition().y - sprite.getHeight() / 2f
            );
        }
    }

    public void draw(SpriteBatch batch) {
        if (ativo && body != null) {
            sprite.draw(batch);
        }
    }
}

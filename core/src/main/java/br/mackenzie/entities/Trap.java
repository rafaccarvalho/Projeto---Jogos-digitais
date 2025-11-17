package br.mackenzie.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

public class Trap extends Collectible {

    public Trap(World world, float xpx, float ypx) {
        super(world, "ratoeira.png", 20f, 30f, xpx, ypx);
    }

    @Override
    protected Shape getShape(float widthPx, float heightPx) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox((widthPx / 2f) / PPM, (heightPx / 2f) / PPM);
        return shape;
    }

    @Override
    public void respawnRandom() {
        float margin = 100;
        float worldWidth = 3000;
        float sizeWidth = 120;
        float x = MathUtils.random(margin, worldWidth - sizeWidth - margin);
        float groundY = 100;
        float sizeHeight = 60;
        float y = groundY + sizeHeight / 2f;
        recreateBody(x, y);
    }
}

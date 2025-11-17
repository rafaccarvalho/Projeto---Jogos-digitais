package br.mackenzie.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.PolygonShape; // Adicionada se necessário

public class Cheese extends Collectible {

    private final float groundY = 100;
    private final float worldWidth = 3200;
    private final float sizeWidth = 50;

    public Cheese(World world, float xpx, float ypx) {
        super(world, "queijo.png",
            50f, 40f, xpx, ypx); // Tamanho em Pixels
    }

    @Override
    protected Shape getShape(float widthPx, float heightPx) {

        CircleShape shape = new CircleShape();
        shape.setRadius((widthPx / 2f) / PPM);
        return shape;
    }

    @Override
    public void respawnRandom() {
        float margin = 60;
        float x_px = MathUtils.random(margin, worldWidth - sizeWidth - margin);
        float y_px = MathUtils.random(groundY + 80, groundY + 80);

        recreateBody(x_px, y_px);
    }
}

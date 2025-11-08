package br.mackenzie.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.Vector2; // Adicionada se necessário

public class Trap extends Collectible {

    private final float groundY = 100;
    private final float worldWidth = 3000;
    private final float sizeWidth = 120;
    private final float sizeHeight = 60;

    public Trap(World world, float xpx, float ypx) {
        super(world, "ratoeira.png",
            90f, 60f, // Tamanho em Pixels
            xpx, ypx);
    }

    @Override
    protected Shape getShape(float widthPx, float heightPx) {
        PolygonShape shape = new PolygonShape();
        // Meia-largura e meia-altura do Box (em metros)
        shape.setAsBox((widthPx / 2f) / PPM, (heightPx / 2f) / PPM);
        return shape;
    }

    @Override
    public void respawnRandom() {
        float margin = 100;
        float x_px = MathUtils.random(margin, worldWidth - sizeWidth - margin);
        float y_px = groundY + (sizeHeight / 2f); // Centraliza no groundY

        // Recria o corpo na nova posição
        recreateBody(x_px, y_px);
    }

    // Armadilhas geralmente não são coletadas e não desaparecem, mas usamos Collectible.collect()
    // no ContactListener para aplicar a lógica de dano.
}

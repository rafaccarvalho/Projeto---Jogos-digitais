package br.mackenzie.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Trap {
    private final Texture texture;
    private final Sprite sprite;
    private final float groundY = 100;
    private final float worldWidth = 3000;

    public Trap() {
        texture = new Texture("ratoeira.png");
        sprite = new Sprite(texture);
        sprite.setSize(120, 60);
        respawn();
    }

    public void respawn() {
        float margin = 100;
        float x = MathUtils.random(margin, worldWidth - sprite.getWidth() - margin);
        sprite.setPosition(x, groundY);
    }

    public boolean checkCollision(Sprite rato) {
        return rato.getBoundingRectangle().overlaps(sprite.getBoundingRectangle());
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void dispose() { texture.dispose(); }
}

package br.mackenzie.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class RottenCheese {
    private final Texture texture;
    private final Sprite sprite;
    private boolean ativo = true;
    private final float groundY = 100;
    private final float worldWidth = 3000;

    public RottenCheese() {
        texture = new Texture("queijoestragado.png");
        sprite = new Sprite(texture);
        sprite.setSize(50, 40);
        respawn();
    }

    public void respawn() {
        float margin = 50;
        float x = MathUtils.random(margin, worldWidth - sprite.getWidth() - margin);
        float y = MathUtils.random(groundY + 80, groundY + 200);
        sprite.setPosition(x, y);
        ativo = true;
    }

    public boolean checkCollision(Sprite rato) {
        if (!ativo) return false;
        if (rato.getBoundingRectangle().overlaps(sprite.getBoundingRectangle())) {
            ativo = false;
            respawn();
            return true;
        }
        return false;
    }

    public void draw(SpriteBatch batch) {
        if (ativo)
            sprite.draw(batch);
    }

    public void dispose() { texture.dispose(); }
}

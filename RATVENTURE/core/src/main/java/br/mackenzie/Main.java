package br.mackenzie;

import com.badlogic.gdx.Game;
import br.mackenzie.screens.StartScreen;

public class Main extends Game {

    @Override
    public void create() {
        setScreen(new StartScreen(this));
    }
}

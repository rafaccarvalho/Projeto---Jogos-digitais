package br.mackenzie.screens;

import br.mackenzie.Main;

public class GameScreen1 extends BaseGameScreen {
    public GameScreen1(Main game) { super(game); }

    @Override protected String getMapPath() { return "esgoto.tmx"; }
    @Override protected BaseGameScreen newInstance() { return new GameScreen1(game); }
    @Override protected BaseGameScreen getNextScreen() { return new GameScreen2(game); }
}

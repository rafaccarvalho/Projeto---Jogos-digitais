package br.mackenzie;

import br.mackenzie.screens.*;
import com.badlogic.gdx.Game;

public class Main extends Game {
    public int pontuacaoGlobal = 0;
    @Override
    public void create() {
        setScreen(new StartScreen(this));
    }}

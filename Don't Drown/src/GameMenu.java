import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PVector;

public class GameMenu {

    public static final int TEXT_COLOUR = 0xFF000000;
    public static final int HIGHLIGHTER_COLOUR = 0xFF00FF00;

    private static class ClickableText {

        final String text;
        final int alignment;

        PVector pos; // bottom left/right
        float width, height;

        ClickableText(String text, int alignment) {
            this.text = text;
            this.alignment = alignment;
        }

        boolean hover(DontDrown sketch) {
            switch (alignment) {
                case PConstants.RIGHT:
                    return sketch.mouseX <= pos.x && sketch.mouseX >= pos.x - width &&
                            sketch.mouseY <= pos.y && sketch.mouseY >= pos.y - height;
                case PConstants.LEFT:
                default:
                    return sketch.mouseX >= pos.x && sketch.mouseX <= pos.x + width &&
                            sketch.mouseY <= pos.y && sketch.mouseY >= pos.y - height;
            }
        }

        void render(DontDrown sketch) {
            if (hover(sketch)) {
                sketch.fill(HIGHLIGHTER_COLOUR);
            } else {
                sketch.fill(TEXT_COLOUR);
            }

            float padding = sketch.textDescent();
            sketch.text(text, pos.x, pos.y + padding);
        }

    }

    private static class Menu {

        static ClickableText back = new ClickableText("Back", PConstants.RIGHT);

        final String title;
        final boolean backable; // true if the page should have a "back" option

        ArrayList<String> nonClickables = new ArrayList<>();
        ArrayList<ClickableText> clickables = new ArrayList<>();

        Menu(String title, boolean backable) {
            this.title = title;
            this.backable = backable;
        }

        static Menu getPauseMenu() {
            Menu menu = new Menu("Paused", false);
            menu.clickables.add(new ClickableText("• Resume", PConstants.LEFT));
            menu.clickables.add(new ClickableText("• Instructions", PConstants.LEFT));
            return menu;
        }

        static Menu getInstructionsMenu() {
            Menu menu = new Menu("Instructions", true);
            menu.nonClickables.add("• Up to jump when on a platform");
            menu.nonClickables.add("• Left and Right to accelerate");
            menu.nonClickables.add("• P to pause");
            menu.nonClickables.add("• Esc to quit");
            menu.nonClickables.add("");
            menu.nonClickables.add("• Reach the top platform to complete the level");
            menu.nonClickables.add("• Keep the wave out of sight to de-stress");
            menu.nonClickables.add("• Collect tokens along the way");
            return menu;
        }

        static Menu getLevelSelectorMenu() {
            Menu menu = new Menu("Level Selector", true);
            return menu;
        }

        static Menu getMainMenu() {
            Menu menu = new Menu("Don't Drown", false);
            menu.clickables.add(new ClickableText("• Instructions", PConstants.LEFT));
            menu.clickables.add(new ClickableText("• Level Selector", PConstants.LEFT));
            return menu;
        }

        void populateClickables(DontDrown sketch, Page page) {
            float y = page.topLineY + page.lineGap;
            float width = sketch.width - 2 * Page.marginX;
            back.pos = new PVector(Page.marginX, y);
            back.width = Page.marginX;
            back.height = page.lineGap;

            // account for title
            y += page.lineGap;
            y += page.lineGap;

            // account for non-clickable text
            y += page.lineGap * nonClickables.size();

            // set clickable fields
            for (ClickableText clickable : clickables) {
                clickable.pos = new PVector(Page.marginX, y);
                clickable.width = width;
                clickable.height = page.lineGap;
                y += page.lineGap;
            }

        }

        public void resolveClick(DontDrown sketch, GameMenu gameMenu) {
            if (backable && back.hover(sketch)) {
                sketch.gameMenu.menuState = sketch.gameMenu.midLevel ? MenuState.PAUSE_MENU : MenuState.MAIN_MENU;
            }

            int i = 0;
            for (ClickableText clickable : clickables) {
                if (clickable.hover(sketch)) {
                    switch (gameMenu.menuState) {
                        case LEVEL_SELECTION:
                            sketch.startLevel(sketch.levels[i]);
                            break;
                        case MAIN_MENU:
                            if (i == 0) {
                                gameMenu.menuState = MenuState.INSTRUCTIONS;
                            } else if (i == 1) {
                                gameMenu.menuState = MenuState.LEVEL_SELECTION;
                            }
                            break;
                        case INSTRUCTIONS:
                            break;
                        case PAUSE_MENU:
                            if (i == 0) {
                                sketch.gameState = DontDrown.GameState.MID_LEVEL;
                            } else {
                                gameMenu.menuState = MenuState.INSTRUCTIONS;
                            }
                            break;
                    }
                }
                i++;
            }
        }

        void render(DontDrown sketch, Page page) {
            float y = page.topLineY + page.lineGap;

            if (backable) {
                sketch.textSize(page.lineGap);
                sketch.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
                back.render(sketch);
            }

            // render title
            sketch.textSize(page.topLineY * 0.75f);
            sketch.fill(TEXT_COLOUR);
            float padding = sketch.textDescent();
            sketch.textAlign(PConstants.CENTER, PConstants.BOTTOM);
            sketch.text(title, sketch.width / 2f, y + padding);

            y += page.lineGap;
            y += page.lineGap;

            // render non-clickable text
            sketch.textAlign(PConstants.LEFT, PConstants.BOTTOM);
            sketch.fill(TEXT_COLOUR);
            sketch.textSize(page.lineGap);
            padding = sketch.textDescent();
            for (String line : nonClickables) {
                sketch.text(line, Page.marginX, y + padding);
                y += page.lineGap;
            }

            // render clickable text
            for (ClickableText clickable : clickables) {
                clickable.render(sketch);
                y += page.lineGap;
            }

            // render static wave
            y += page.lineGap;
            sketch.staticWave.pos.y = y;
            sketch.staticWave.render();
        }

    }

    public enum MenuState {
        PAUSE_MENU(Menu.getPauseMenu()),
        MAIN_MENU(Menu.getMainMenu()),
        LEVEL_SELECTION(Menu.getLevelSelectorMenu()),
        INSTRUCTIONS(Menu.getInstructionsMenu()),;

        public final Menu menu;

        MenuState(Menu menu) {
            this.menu = menu;
        }
    }

    private final DontDrown sketch;

    public final Page page;

    public boolean midLevel = false;
    public MenuState menuState = MenuState.MAIN_MENU;

    public GameMenu(DontDrown sketch) {
        this.sketch = sketch;
        page = new Page(sketch);

        for (MenuState state : MenuState.values()) {
            state.menu.populateClickables(sketch, page);
        }
    }

    public void resolveClick() {
        menuState.menu.resolveClick(sketch, this);
    }

    public void updateLevelSelector() {
        MenuState.LEVEL_SELECTION.menu.clickables.clear();
        for (Level level : sketch.levels) {
            MenuState.LEVEL_SELECTION.menu.clickables.add(new ClickableText(
                    String.format("• %-25s %d/%d", level.name, level.highScore, level.tokens.size()), PConstants.LEFT));
        }
        MenuState.LEVEL_SELECTION.menu.populateClickables(sketch, page);
    }

    public void render() {
        page.render();

        sketch.colorModeRGB();
        sketch.fill(TEXT_COLOUR);
        sketch.textSize(page.topLineY * 0.25f);
        float padding = sketch.textDescent();
        sketch.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
        sketch.text("190021081", sketch.width, page.topLineY + page.lineGap + padding);

        menuState.menu.render(sketch, page);

    }

}

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

    private static class LineOfText {

        public final ClickableText clickable;
        public final String nonClickable;

        public LineOfText(ClickableText clickableText) {
            this.clickable = clickableText;
            this.nonClickable = null;
        }

        public LineOfText(String nonClickable) {
            this.nonClickable = nonClickable;
            this.clickable = null;
        }

    }

    private static class MenuPage {

        ClickableText back = new ClickableText("Back", PConstants.RIGHT);

        final String title;
        final boolean backable; // true if the page should have a "back" option

        float yOrigin = 0;
        ArrayList<LineOfText> linesOfText = new ArrayList<>();
        Page page;

        MenuPage(String title, boolean backable) {
            this.title = title;
            this.backable = backable;
        }

        static MenuPage getPauseMenu() {
            MenuPage menu = new MenuPage("Paused", false);
            menu.linesOfText.add(new LineOfText(new ClickableText("• Resume", PConstants.LEFT)));
            menu.linesOfText.add(new LineOfText(new ClickableText("• Instructions", PConstants.LEFT)));
            return menu;
        }

        static MenuPage getInstructionsMenu() {
            MenuPage menu = new MenuPage("Instructions", true);
            String[] text = new String[] {
                    "• Up to jump when on a platform",
                    "• Down to fall through a platform",
                    "• Left and Right to accelerate",
                    "• Up to jump when on a platform",
                    "• P to pause",
                    "• Esc to quit",
                    "",
                    "• Reach the top platform to complete the level",
                    "• Keep the wave out of sight to de-stress",
                    "• Collect tokens along the way",
            };
            for (String string : text) {
                menu.linesOfText.add(new LineOfText(string));
            }
            return menu;
        }

        static MenuPage getLevelSelectorMenu() {
            MenuPage menu = new MenuPage("Level Selector", true);
            return menu;
        }

        static MenuPage getMainMenu() {
            MenuPage menu = new MenuPage("Don't Drown", false);
            menu.linesOfText.add(new LineOfText(new ClickableText("• Instructions", PConstants.LEFT)));
            menu.linesOfText.add(new LineOfText(new ClickableText("• Level Selector", PConstants.LEFT)));
            return menu;
        }

        void populateClickables(DontDrown sketch) {
            page = new Page(sketch);

            float y = sketch.scoreOverlay.endOfPadding + page.lineGap;
            float width = sketch.width - 2 * Page.marginX;
            back.pos = new PVector(Page.marginX, y);
            back.width = Page.marginX;
            back.height = page.lineGap;

            // account for title
            y += page.lineGap;
            y += page.lineGap;

            for (LineOfText line : linesOfText) {
                if (line.nonClickable != null) {
                    // account for non-clickable text
                    y += page.lineGap;
                } else {
                    // set clickable fields
                    line.clickable.pos = new PVector(Page.marginX, y);
                    line.clickable.width = width;
                    line.clickable.height = page.lineGap;
                    y += page.lineGap;
                }
            }

        }

        public void resolveClick(DontDrown sketch, GameMenu gameMenu) {
            if (backable && back.hover(sketch)) {
                sketch.gameMenu.setMenuState(sketch.gameMenu.midLevel ? MenuState.PAUSE_MENU : MenuState.MAIN_MENU);
            }

            int i = 0; // clickable index (not the line index)
            for (LineOfText line : linesOfText) {
                ClickableText clickable = line.clickable;
                if (clickable != null) {
                    if (clickable.hover(sketch)) {
                        switch (gameMenu.menuState) {
                            case LEVEL_SELECTION:
                                int debuffIndex = i / Difficulty.values().length;
                                int difficultyIndex = i % Difficulty.values().length;
                                sketch.startLevel(sketch.levels[debuffIndex][difficultyIndex]);
                                break;
                            case MAIN_MENU:
                                if (i == 0) {
                                    gameMenu.setMenuState( MenuState.INSTRUCTIONS);
                                } else if (i == 1) {
                                    gameMenu.setMenuState(MenuState.LEVEL_SELECTION);
                                }
                                break;
                            case INSTRUCTIONS:
                                break;
                            case PAUSE_MENU:
                                if (i == 0) {
                                    sketch.gameState = DontDrown.GameState.MID_LEVEL;
                                } else {
                                    gameMenu.setMenuState(MenuState.INSTRUCTIONS);
                                }
                                break;
                        }
                    }
                    i++;
                }
            }
        }

        void render(DontDrown sketch) {
            page.render();

            float y = yOrigin + sketch.scoreOverlay.endOfPadding + page.lineGap;

            if (backable) {
                sketch.textSize(page.lineGap);
                sketch.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
                back.render(sketch);
            }

            // render title
            sketch.textSize(sketch.scoreOverlay.endOfPadding * 0.75f);
            sketch.fill(TEXT_COLOUR);
            float padding = sketch.textDescent();
            sketch.textAlign(PConstants.CENTER, PConstants.BOTTOM);
            sketch.text(title, sketch.width / 2f, y + padding);

            y += page.lineGap;
            y += page.lineGap;

            // render lines of text
            sketch.textAlign(PConstants.LEFT, PConstants.BOTTOM);
            sketch.textSize(page.lineGap);
            padding = sketch.textDescent();
            for (LineOfText line : linesOfText) {
                if (line.clickable == null) {
                    // render non-clickable text
                    sketch.fill(TEXT_COLOUR);
                    sketch.text(line.nonClickable, Page.marginX, y + padding);
                } else {
                    // render clickable text
                    line.clickable.render(sketch);
                }
                y += page.lineGap;
            }

            // render static wave
            y += page.lineGap;
            sketch.staticWave.pos.y = y;
            sketch.staticWave.render();
        }

    }

    public enum MenuState {
        PAUSE_MENU(MenuPage.getPauseMenu()),
        MAIN_MENU(MenuPage.getMainMenu()),
        LEVEL_SELECTION(MenuPage.getLevelSelectorMenu()),
        INSTRUCTIONS(MenuPage.getInstructionsMenu()),;

        public final MenuPage menuPage;

        MenuState(MenuPage menu) {
            this.menuPage = menu;
        }
    }

    private final DontDrown sketch;

    public boolean midLevel = false;
    private MenuState menuState = MenuState.MAIN_MENU;

    public GameMenu(DontDrown sketch) {
        this.sketch = sketch;

        for (MenuState state : MenuState.values()) {
            state.menuPage.populateClickables(sketch);
        }
    }

    public MenuState getMenuState() {
        return menuState; 
    }

    public void setMenuState(MenuState menuState) {
        this.menuState = menuState;
        resetPage();
    }

    public void resolveClick() {
        menuState.menuPage.resolveClick(sketch, this);
    }

    public void updateLevelSelector() {
        ArrayList<LineOfText> linesOfText = MenuState.LEVEL_SELECTION.menuPage.linesOfText;
        linesOfText.clear();
        int debuffIndex = 0;
        for (Level[] levelBatch : sketch.levels) { // gruoped by debuff
            linesOfText.add(new LineOfText(Debuff.values()[debuffIndex++].label));
            for (Level level : levelBatch) {
                linesOfText.add(new LineOfText(new ClickableText(
                        String.format("     • %-15s %d/%d",
                                level.difficulty.name().replace("_", " "),
                                level.highScore,
                                level.tokens.size()),
                        PConstants.LEFT)));
            }
            linesOfText.add(new LineOfText(""));
        }
        MenuState.LEVEL_SELECTION.menuPage.populateClickables(sketch);

        float minimumHeight = sketch.scoreOverlay.endOfPadding + sketch.height / 3f +
                MenuState.LEVEL_SELECTION.menuPage.page.lineGap * linesOfText.size();

        if (sketch.height < minimumHeight) {
            MenuState.LEVEL_SELECTION.menuPage.page = new Page(sketch, (int) minimumHeight, true);
        }

    }

    private void resetPage() {
        if (menuState.menuPage.page.startAtTop) {
            // scroll back up to top 
                scrollWrapper(menuState.menuPage.page.height); 
        } else {
            //scroll back down to bottom 
            scrollWrapper(-menuState.menuPage.page.height); 
        }
    }

    private void scroll(float count) {
        menuState.menuPage.page.lines.translate(0, count);
        menuState.menuPage.yOrigin += count;
        menuState.menuPage.back.pos.y += count;
        for (LineOfText line : menuState.menuPage.linesOfText) {
            if (line.clickable != null) {
                line.clickable.pos.y += count;
            }
        }
    }

    public void scrollWrapper(int count) {
        try {
            float topLimit = sketch.height - (float) menuState.menuPage.page.height;
            float yOrigin = menuState.menuPage.yOrigin;

            if (count < 0) {
                // scrolling down
                if (yOrigin + count <= topLimit) {
                    scroll(topLimit - yOrigin);
                } else {
                    scroll(count);
                }
            } else {
                // scrolling up
                if (yOrigin + count >= 0f) {
                    scroll(0f - yOrigin);
                } else {
                    scroll(count);
                }
            }

        } catch (NullPointerException e) {
            // do nothing; scrolled in loading screen
        }
    }

    public void render() {
        menuState.menuPage.render(sketch);
    }

}

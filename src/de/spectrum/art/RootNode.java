package de.spectrum.art;

import de.spectrum.App;
import de.spectrum.gui.OnFocusChangedListener;
import de.spectrum.gui.java.Component;
import de.spectrum.gui.java.RootMenu;
import de.spectrum.gui.processing.OnClickListener;
import de.spectrum.gui.processing.RootView;
import de.spectrum.gui.processing.View;
import de.spectrum.gui.processing.buttons.DeleteButton;
import de.spectrum.gui.processing.buttons.PlusButton;

/**
 * Encapsulates all data structures and methods linked with a root node.
 */
public class RootNode extends Node {
    private int currentFrame = 0;

    public RootNode(int xCenter, int yCenter, App context) {
        super(null, context);

        final RootView rootView = new RootView(xCenter, yCenter, context);
        rootView.addOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isFocused()) RootNode.this.context.setFocusedComponent(null);
                else RootNode.this.context.setFocusedComponent(v);
            }
        });
        registerMouseObserver(rootView);
        setProcessingView(rootView);

        context.addOnFocusChangedListener(new OnFocusChangedListener() {
            @Override
            public void onFocusChanged(Component c) {
                // delegate
                rootView.onFocusChanged(c);

                if (rootView.isFocused()) {
                    // show command node ui
                    setChildNodeVisibility(true);
                } else {
                    // hide command node ui
                    setChildNodeVisibility(false);
                    // re-show this node as setting al nodes invisible also affected this node
                    getProcessingView().setVisible(true);
                }
            }
        });

        final PlusButton plusButton = new PlusButton(2 * rootView.getWidth() / 3, 2 * rootView.getHeight() / 3,
                rootView.getWidth() / 2, rootView.getHeight() / 2, context);
        plusButton.addOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // auto-focus this node
                RootNode.this.context.setFocusedComponent(rootView);

                CommandNode cmdNode = new CommandNode(RootNode.this, RootNode.this.context);
                // only show the child nodes when this node is focused
                cmdNode.setChildNodeVisibility(rootView.isFocused());
                addNextNode(cmdNode);
                rearrangeChildNodes();
            }
        });
        rootView.addView(plusButton);

        final DeleteButton deleteButton = new DeleteButton(2 * rootView.getWidth() / 3, 0,
                rootView.getWidth() / 2, rootView.getHeight() / 2, context);
        deleteButton.addOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // un-focus to hide the menu
                if(rootView.isFocused()) RootNode.this.context.setFocusedComponent(null);
                // mark the node as delted. This will also mark all sub-nodes as deleted
                RootNode.this.context.onDelete(RootNode.this);
            }
        });
        rootView.addView(deleteButton);

        RootMenu menu = new RootMenu(context, this);
        setMenuView(menu);
        context.addOnFocusChangedListener(menu);

        // setSettingsView(settingsView);
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void decCurrentFrame() {
        if (currentFrame > 0) currentFrame--;
    }

    public void deleteCommandNode(Node commandNode) {
        commandNode.delete();
    }

    @Override
    protected void render() {
        // do nothing
    }
}

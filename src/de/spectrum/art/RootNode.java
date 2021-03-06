package de.spectrum.art;

import de.spectrum.App;
import de.spectrum.gui.OnFocusChangedListener;
import de.spectrum.gui.java.*;
import de.spectrum.gui.java.Component;
import de.spectrum.gui.processing.OnClickListener;
import de.spectrum.gui.processing.RootView;
import de.spectrum.gui.processing.View;
import de.spectrum.gui.processing.buttons.DeleteButton;
import de.spectrum.gui.processing.buttons.PlusButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Encapsulates all data structures and methods linked with a root node.
 *
 * @author Giorgio Gross <lars.ordsen@gmail.com>
 */
public class RootNode extends Node implements OnPaintContextChangedListener {
    private int currentFrame = 0;
    private int childNodeIdCounter = 1;
    private PaintContext paintContext;

    public RootNode(int xCenter, int yCenter, App context) {
        super(null, context);
        setId(context.getNewRootNodeId());

        final RootView rootView = new RootView(xCenter, yCenter, getId(), context);
        rootView.addOnClickListener(v -> {
            if (v.isFocused()) RootNode.this.context.setFocusedComponent(null);
            else RootNode.this.context.setFocusedComponent(v);
        });
        registerMouseObserver(rootView);
        setProcessingView(rootView);

        context.addOnFocusChangedListener(c -> {
            // delegate
            rootView.onFocusChanged(c);

            if (rootView.isFocused()) {
                // show command node ui
                setChildNodeVisibility(true, new ArrayList<Node>());
            } else {
                hideUI(new ArrayList<Node>());
            }
        });

        final PlusButton plusButton = new PlusButton(rootView.getWidth() / 2, rootView.getHeight() / 2,
                rootView.getWidth() / 2, rootView.getHeight() / 2, context);
        plusButton.addOnClickListener(v -> {
            // auto-focus this node
            RootNode.this.context.setFocusedComponent(rootView);

            CommandNode cmdNode = new CommandNode(RootNode.this, RootNode.this.context);
            // only show the child nodes when this node is focused
            cmdNode.setChildNodeVisibility(rootView.isFocused(), new ArrayList<Node>());
            addNextNode(cmdNode);
            rearrangeChildNodes(new ArrayList<Node>());
        });
        rootView.addView(plusButton);

        final DeleteButton deleteButton = new DeleteButton(rootView.getWidth() / 2, 0,
                rootView.getWidth() / 2, rootView.getHeight() / 2, context);
        deleteButton.addOnClickListener(v -> {
            // un-focus to hide the menu
            if(rootView.isFocused()) RootNode.this.context.setFocusedComponent(null);
            // mark the node as delted. This will also mark all sub-nodes as deleted
            RootNode.this.context.onDelete(RootNode.this);
        });
        rootView.addView(deleteButton);

        RootMenu menu = new RootMenu(context, this);
        setMenuView(menu);
        context.addOnFocusChangedListener(menu);

        paintContext = new PaintContext(this, xCenter, yCenter);
        paintContext.register(this);

        RootSettingsMenu settingsMenu = new RootSettingsMenu(context, this, getPaintContext());
        setSettingsView(settingsMenu);
        context.addOnFocusChangedListener(settingsMenu);
    }

    public int getNewChildNodeId() {
        return childNodeIdCounter++;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void decCurrentFrame() {
        if (currentFrame > 0) currentFrame--;
    }

    public void incCurrentFrame() {
        currentFrame++;
    }

    public void deleteCommandNode(Node commandNode) {
        commandNode.delete();
    }

    public PaintContext getPaintContext() {
        return paintContext;
    }

    public void updatePosition() {
        getProcessingView().setX(paintContext.getCursor().getxBase());
        getProcessingView().setY(paintContext.getCursor().getyBase());
        getMenuView().validateLocation();
        rearrangeChildNodes(new ArrayList<>());
    }



    @Override
    protected void render() {
        incCurrentFrame();
    }

    @Override
    protected void hideCustomUI() {
        // hide command node ui
        setChildNodeVisibility(false, new ArrayList<Node>());
        // re-show this node as setting all nodes invisible also affected this node
        getProcessingView().setVisible(true);

        getMenuView().setFrameVisibility(false);
        getSettingsView().setFrameVisibility(false);
    }

    @Override
    public void onPaintContextChanged() {
        updatePosition();
    }
}

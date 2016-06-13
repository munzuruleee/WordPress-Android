package org.wordpress.android.ui.menus.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ViewFlipper;

import org.wordpress.android.R;
import org.wordpress.android.models.MenuItemModel;
import org.wordpress.android.ui.menus.items.BaseMenuItemEditor;
import org.wordpress.android.ui.menus.items.MenuItemEditorFactory;
import org.wordpress.android.ui.menus.items.MenuItemEditorFactory.ITEM_TYPE;

/**
 * A combination of views that provide functionality for editing menu items.
 *
 * TEMPORARY
 *  - {@link Spinner} containing a list of available item types
 *
 *  MAYBE PERMANENT:
 *  - {@link ViewFlipper} containing stubs for each of the item edit views
 *  - {@link Button}'s for canceling and saving/updating the current item
 */
public class MenuItemEditView extends LinearLayout {
    public interface MenuItemEditorListener {
        void onEditorShown();
        void onEditorHidden();
        void onMenuItemAdded(MenuItemModel menuItem);
        void onMenuItemChanged(MenuItemModel menuItem);
    }

    private ITEM_TYPE mType;
    private MenuItemEditorListener mListener;
    private MenuItemModel mOriginalItem;

    private Spinner mTypePicker;
    private Button mAddButton;
    private Button mCancelButton;
    private ViewFlipper mEditorFlipper;

    public MenuItemEditView(Context context, ITEM_TYPE type) {
        super(context);
        initView();
        setType(type);
    }

    public MenuItemEditView(Context context, @NonNull MenuItemModel originalItem) {
        super(context);
        initView();
        mOriginalItem = originalItem;
        setType(MenuItemEditorFactory.ITEM_TYPE.typeForString(originalItem.type));
    }

    public MenuItemEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        setType(ITEM_TYPE.NULL);
    }

    public void setListener(MenuItemEditorListener listener) {
        mListener = listener;
    }

    private void initView() {
        inflate(getContext(), R.layout.menu_item_edit_view, this);
        mTypePicker = (Spinner) findViewById(R.id.menu_item_type_spinner);
        mAddButton = (Button) findViewById(R.id.menu_item_edit_add);
        mCancelButton = (Button) findViewById(R.id.menu_item_edit_cancel);
        mEditorFlipper = (ViewFlipper) findViewById(R.id.menu_item_editor_flipper);

        // add editor views
        for (ITEM_TYPE type : ITEM_TYPE.values()) {
            BaseMenuItemEditor editor = MenuItemEditorFactory.getEditor(getContext(), type);
            if (editor != null) {
                mEditorFlipper.addView(editor);
            }
        }

        mTypePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO: set type and update editor view
                mEditorFlipper.setDisplayedChild(position);
                ViewGroup child = (ViewGroup) mEditorFlipper.getCurrentView();
                View stub = child.getChildAt(0);
                if (stub instanceof ViewStub) {
                    ((ViewStub) stub).inflate();
                }
                ITEM_TYPE type = ITEM_TYPE.typeForString(mTypePicker.getSelectedItem().toString());
                setType(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });

        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                notifyHidden();
            }
        });
        if (mOriginalItem != null) {
            mAddButton.setText(getContext().getString(R.string.update_verb));
        }
        mAddButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: add menu item to current menu
                if (mOriginalItem != null) {
                    notifyChanged(mOriginalItem);
                } else {
                    notifyAdded(null);
                }
            }
        });
    }

    public void setType(ITEM_TYPE type) {
        mType = type;
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    //
    // Listener callbacks
    //
    private void notifyShown() {
        if (mListener != null) mListener.onEditorShown();
    }

    private void notifyHidden() {
        if (mListener != null) mListener.onEditorHidden();
    }

    private void notifyAdded(MenuItemModel menuItem) {
        if (mListener != null) mListener.onMenuItemAdded(menuItem);
    }

    private void notifyChanged(MenuItemModel menuItem) {
        if (mListener != null) mListener.onMenuItemChanged(menuItem);
    }
}

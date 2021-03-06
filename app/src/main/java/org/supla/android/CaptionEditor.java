package org.supla.android;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.supla.android.lib.SuplaClientMessageHandler;
import org.supla.android.lib.SuplaClientMsg;

public abstract class CaptionEditor implements View.OnClickListener, TextWatcher, SuperuserAuthorizationDialog.OnAuthorizarionResultListener {
    private Context context;
    private Button btnCancel;
    private Button btnOK;
    private AlertDialog dialog;
    private EditText edCaption;
    private TextView tvTitle;
    private int id;
    private String originalCaption;
    SuperuserAuthorizationDialog superuserAuthorizationDialog;

    public CaptionEditor(Context context) {
        this.context = context;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.caption_editor_dialog, null);

        btnCancel = v.findViewById(R.id.btnDialogCancel);
        btnCancel.setOnClickListener(this);

        btnOK = v.findViewById(R.id.btnDialogOK);
        btnOK.setOnClickListener(this);

        edCaption = v.findViewById(R.id.edDialogCaption);
        edCaption.addTextChangedListener(this);
        tvTitle = v.findViewById(R.id.tvDialogCaptionTitle);

        builder.setView(v);
        dialog = builder.create();
    }

    public void edit(int id) {
        this.id = id;

        if (superuserAuthorizationDialog == null) {
            superuserAuthorizationDialog = new SuperuserAuthorizationDialog(getContext());
            superuserAuthorizationDialog.setOnAuthorizarionResultListener(this);
        }

        superuserAuthorizationDialog.showIfNeeded();
    }

    @Override
    public void onClick(View v) {
        if (dialog == null) {
            return;
        }

        if (v == btnCancel) {
            dialog.cancel();
        } else if (v == btnOK) {
            if ((originalCaption == null && edCaption.getText() != null)
                    || (originalCaption != null && edCaption.getText() == null)
                    || (originalCaption != null
                        && edCaption.getText() != null
                        && !originalCaption.equals(edCaption.getText().toString()))) {
                applyChanged(edCaption.getText().toString());

                SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onDataChanged);
                SuplaClientMessageHandler.getGlobalInstance().sendMessage(msg);
            }
            dialog.dismiss();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        btnOK.setEnabled(edCaption.getText().length() >= getMinLength()
                && edCaption.getText().length() <= getMaxLength());
    }

    protected int getMinLength() {
        return 0;
    }

    protected int getMaxLength() {
        return 100;
    }

    protected int getHint() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public Context getContext() {
        return context;
    }

    protected abstract int getTitle();
    protected abstract String getCaption();
    protected abstract void applyChanged(String newCaption);

    @Override
    public void onSuperuserOnAuthorizarionResult(SuperuserAuthorizationDialog dialog,
                                                 boolean Success, int Code) {
        if (!Success) {
            return;
        }

        dialog.close();

        originalCaption = getCaption();
        edCaption.setText(originalCaption);

        if (getHint() != 0) {
            edCaption.setHint(getHint());
        } else {
            edCaption.setHint("");
        }
        tvTitle.setText(getTitle());

        afterTextChanged(edCaption.getText());

        if (this.dialog != null) {
            this.dialog.show();
        }
    }

    @Override
    public void authorizationCanceled() {

    }
}

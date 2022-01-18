package com.example.tecsicomappdrivers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Custom_Dialog extends AppCompatDialogFragment {

    String header;
    String body;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable  Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.layout_dialog,null);

        builder.setView(view)
                .setTitle("Ingresa una Referencia de tu Ubicacion")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Solicitar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Inicio inicio = new Inicio();

                        if (getArguments() != null) {
                            header = getArguments().getString("latitud","");
                            body = getArguments().getString("body","");
                        }

                        EditText etReferencia;

                        etReferencia=view.findViewById(R.id.etReferencia);

                        String referencia=etReferencia.getText().toString();

                        Toast.makeText(getContext(), "OKEY ENVIALO ::"+referencia+"::"+header, Toast.LENGTH_SHORT).show();



                    }
                });



        return builder.create();
    }
}

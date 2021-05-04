package com.ispgaya.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistarActivity extends AppCompatActivity {

    EditText mEmailET, mPasswordET;
    Button mFinalizarRegisto;
    TextView mTemConta;

    //barra de progresso enquanto ocorre o registo
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registar);

        //Actionbar com titulo
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Criar conta");
        //ativar botão de voltar atrás
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //definir variaveis

        mEmailET = findViewById(R.id.emailET);
        mPasswordET = findViewById(R.id.passwordET);
        mFinalizarRegisto = findViewById(R.id.botao_finalizar_registo);
        mTemConta = findViewById(R.id.tem_conta_TV);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("A registar-te...");

        //clique do botao finalizar registo
        mFinalizarRegisto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email e password
                String email = mEmailET.getText().toString().trim();
                String password = mPasswordET.getText().toString().trim();
                //validação
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //surgir erro e focar no email
                    mEmailET.setError("Email inválido!");
                    mEmailET.setFocusable(true);
                }
                else if (password.length()<6){
                    //surgir eurro e focar na password
                    mPasswordET.setError("A password tem que ter pelo menos 6 caractéres.");
                    mPasswordET.setFocusable(true);
                }
                else {
                    registarUtilizador(email, password); //registar o utilizador
                }
            }
        });

        //clique no texto de Ja tem conta
        mTemConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistarActivity.this, IniciarSessaoActivity.class));
            }
        });
    }

    private void registarUtilizador(String email, String password) {
        //o email e password são válidos, mostrar progress dialog e iniciar sessao
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            //obter email de utilizador e uid a partir do registo
                            String email = user.getEmail();
                            String uid = user.getUid();
                            //quando o utilizador é registado é necessário inserir os dados na base de dados
                            //usamos um hashmap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("nome", "");
                            hashMap.put("telemovel", "");
                            hashMap.put("imagem", "");
                            //firabase instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //caminho para a inserção de dados chamado "Utilizadores"
                            DatabaseReference reference = database.getReference("Utilizadores");
                            //inserir dados a partir do hashmap para a base de dados
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegistarActivity.this, "Registado com sucesso!\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegistarActivity.this, DashboardActivity.class));
                            finish();
                        }
                        else {
                            progressDialog.dismiss();
                            Toast.makeText(RegistarActivity.this, "O registo falhou.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegistarActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //voltar atras
        return super.onSupportNavigateUp();
    }
}
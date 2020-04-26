package com.sendby.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.common.componentes.fragment.LazyInitFragment;
import com.sendby.Constants;
import com.sendby.widget.MSignInButton;
import com.flybd.sharebox.BuildConfig;
import com.flybd.sharebox.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

public class LoginFragment extends LazyInitFragment {

    private static final int REQUEST_LOGIN_CODE = 426;

    private static final String TAG = "LoginFragment";

    private MSignInButton btnSignIn;

    private GoogleSignInClient googleSignInClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("598282183161-hj1b5kphkr1u637cubn7tm3ki48rj43i.apps.googleusercontent.com")
                .requestEmail()
                .build();
        // [START build_client]
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
        // [END build_client]
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnSignIn = view.findViewById(R.id.btn_sign);
        btnSignIn.setOnClickListener(v -> {
            if (btnSignIn.getSignInButton().getText().toString().equalsIgnoreCase("登出")) {
                signOut();
            } else {
                signIn();
            }
        });
        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (account != null) {
            //login already
            if (!account.isExpired()) {
                Constants.get().setToken("");
                btnSignIn.getSignInButton().setText("登出");
            }
        }
    }

    // [START signIn]
    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_LOGIN_CODE);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Activity activity = getActivity();
        if (activity != null) {
            googleSignInClient.signOut()
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Constants.get().setToken("");
                            Intent intent = new Intent();
                            activity.setResult(Activity.RESULT_OK, intent);
                            activity.finish();
                        }
                    });
        }
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        googleSignInClient.revokeAccess()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN_CODE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "signInResult:" + new Gson().toJson(account));
            }
            if (account != null) {
                Activity activity = getActivity();
                Constants.get().setToken(account.getIdToken());
                if (activity != null) {
                    Intent intent = new Intent();
                    intent.putExtra("data", account);
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.finish();
                }
            } else {
                Context ctx = getContext();
                if (ctx != null) {
                    Toast.makeText(ctx, "登录失败，请重试", Toast.LENGTH_LONG).show();
                }
            }
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            e.printStackTrace();
            Context ctx = getContext();
            if (ctx != null) {
                Toast.makeText(ctx, "登录失败，请重试", Toast.LENGTH_LONG).show();
            }
        }
    }
    // [END handleSignInResult]
}

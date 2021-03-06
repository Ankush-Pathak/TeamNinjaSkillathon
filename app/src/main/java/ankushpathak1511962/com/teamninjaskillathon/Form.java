package ankushpathak1511962.com.teamninjaskillathon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.DropBoxManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class Form extends AppCompatActivity {
    TextInputLayout editTextSerialNo, editTextFirstName, editTextPhoneNo, editTextEmail, editTextLastName;
    TextView textViewSerialNo;
    Spinner spinnerCountryCode;
    Calendar calendar;
    Button buttonSubmit;
    int currentSerial = 1;
    DatabaseReference databaseReference, databaseReferenceCounter, databaseReferenceDay;


    String serial = "";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        initialise();

        onTextChangeListeners();

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check if any field is empty
                if(editTextFirstName.getEditText().getText().toString().length() == 0)
                {
                    Toast.makeText(Form.this,"First name empty",Toast.LENGTH_SHORT).show();
                    editTextFirstName.requestFocus();
                    return;
                }
                else if(editTextLastName.getEditText().getText().toString().length() == 0)
                {
                    Toast.makeText(Form.this,"Last name empty",Toast.LENGTH_SHORT).show();
                    editTextLastName.requestFocus();
                    return;
                }
                else if(editTextPhoneNo.getEditText().getText().toString().length() == 0)
                {
                    Toast.makeText(Form.this,"Contact empty",Toast.LENGTH_SHORT).show();
                    editTextPhoneNo.requestFocus();
                    return;
                }
                else if(editTextEmail.getEditText().getText().toString().length() == 0)
                {
                    Toast.makeText(Form.this,"Email empty",Toast.LENGTH_SHORT).show();
                    editTextEmail.requestFocus();
                    return;
                }

                //Validate field values
                if(verifyEmail() && verifyFirstName() && verifyLastName() && verifyPhone()) {
                    Entry entry = new Entry();
                    entry.setName(formatName(editTextFirstName.getEditText().getText().toString()) + " " + formatName(editTextLastName.getEditText().getText().toString()));
                    entry.setEmail(editTextEmail.getEditText().getText().toString());
                    entry.setPhoneNo(spinnerCountryCode.getSelectedItem().toString()+editTextPhoneNo.getEditText().getText().toString());
                    entry.setSerialNo(serial);

                    databaseReference.push().setValue(entry);
                    databaseReferenceCounter.setValue(currentSerial+1);

                    Toast.makeText(Form.this,"Done",Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(Form.this,MainActivity.class));
                    finish();
                }


                if(!verifyLastName())
                {
                    Toast.makeText(Form.this,"Error in field last name",Toast.LENGTH_SHORT).show();
                }
                if(!verifyFirstName())
                {
                    Toast.makeText(Form.this,"Error in field first name",Toast.LENGTH_SHORT).show();
                }
                if(!verifyEmail())
                {
                    Toast.makeText(Form.this,"Error in field email",Toast.LENGTH_SHORT).show();
                }
                if(!verifyPhone())
                {
                    Toast.makeText(Form.this,"Error in field contact",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    void initialise()
    {
        editTextEmail = (TextInputLayout)findViewById(R.id.editTextEmail);
        editTextFirstName = (TextInputLayout)findViewById(R.id.editTextFirstName);
        editTextLastName = (TextInputLayout)findViewById(R.id.editTextLastName);
        editTextPhoneNo = (TextInputLayout)findViewById(R.id.editTextPhoneNo);
        editTextSerialNo = (TextInputLayout)findViewById(R.id.editTextSerialNo);
        textViewSerialNo = (TextView)findViewById(R.id.textViewSerialNo);
        buttonSubmit = (Button)findViewById(R.id.buttonSubmit);
        spinnerCountryCode = (Spinner)findViewById(R.id.spinnerCountryCode);

        populateSpinner();

        progressDialog = new ProgressDialog(Form.this);
        progressDialog.setTitle("Generating Serial No.");
        progressDialog.setMessage("Please wait\nEnsure a working internet connection");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        //To ensure no one can submit before serial no is generated
        editTextLastName.setEnabled(false);
        editTextSerialNo.setEnabled(false);
        editTextPhoneNo.setEnabled(false);
        editTextEmail.setEnabled(false);
        editTextFirstName.setEnabled(false);
        buttonSubmit.setEnabled(false);

        //Firebase init
        databaseReference = FirebaseDatabase.getInstance().getReference().child("TeamNinja");
        databaseReferenceCounter = FirebaseDatabase.getInstance().getReference().child("counter");
        databaseReferenceDay = FirebaseDatabase.getInstance().getReference().child("day");
        databaseReference.keepSynced(true);
        databaseReferenceCounter.keepSynced(true);
        databaseReferenceDay.keepSynced(true);

        //Generating serial no and putting it on edit text and text view
        databaseReferenceDay.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int dayFromFB = dataSnapshot.getValue(Integer.class);
                calendar = Calendar.getInstance();
                if((int)calendar.get(Calendar.DAY_OF_MONTH) != dayFromFB)
                {
                    databaseReferenceCounter.setValue(1);
                    databaseReferenceDay.setValue(calendar.get(Calendar.DAY_OF_MONTH));

                    //Add serial no to edit text and text view and enable views
                    setup();
                }
                else
                {
                    databaseReferenceCounter.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            currentSerial = dataSnapshot.getValue(Integer.class);
                            //Add serial no to edit text and text view and enable views
                            setup();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void onTextChangeListeners()
    {
        //Clear errors of editTexts which are out of focus, keep errors if necessary
        editTextPhoneNo.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                    clearErrors();
            }
        });
        editTextEmail.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                    clearErrors();
            }
        });
        editTextLastName.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editTextLastName.getEditText().setText(formatName(editTextLastName.getEditText().getText().toString()));
                clearErrors();
            }
        });
        editTextFirstName.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editTextFirstName.getEditText().setText(formatName(editTextFirstName.getEditText().getText().toString()));
                clearErrors();
            }
        });

        //To show errors after validation of email and repeating for other fields
        editTextEmail.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //clearErrors();
                if(verifyEmail())
                {
                    editTextEmail.setError("Email valid");
                    editTextEmail.setErrorTextAppearance(R.style.ErrorCorrect);
                }
                else
                {
                    editTextEmail.setError("Email invalid");
                    editTextEmail.setErrorTextAppearance(R.style.ErrorInCorrect);
                }
            }
        });

        editTextPhoneNo.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //clearErrors();
                if(verifyPhone())
                {
                    editTextPhoneNo.setError("Phone valid");
                    editTextPhoneNo.setErrorTextAppearance(R.style.ErrorCorrect);
                }
                else
                {
                    editTextPhoneNo.setError("Phone invalid");
                    editTextPhoneNo.setErrorTextAppearance(R.style.ErrorInCorrect);
                }
            }
        });

        editTextFirstName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString();
                if(name.length()!=0) {
                    if (Character.isSpaceChar(name.charAt(name.length() - 1))) {
                        editTextLastName.requestFocus();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(verifyFirstName()){
                    editTextFirstName.setError("First name valid");
                    editTextFirstName.setErrorTextAppearance(R.style.ErrorCorrect);
                }
                else {
                    editTextFirstName.setError("First name invalid");
                    editTextFirstName.setErrorTextAppearance(R.style.ErrorInCorrect);
                }
            }
        });

        editTextLastName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString();
                if(name.length()!=0) {
                    if (Character.isSpaceChar(name.charAt(name.length() - 1))) {
                        editTextPhoneNo.requestFocus();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(verifyLastName()){
                    editTextLastName.setError("Last name valid");
                    editTextLastName.setErrorTextAppearance(R.style.ErrorCorrect);
                }
                else {
                    editTextLastName.setError("Last name invalid");
                    editTextLastName.setErrorTextAppearance(R.style.ErrorInCorrect);
                }
            }
        });
    }


    void setup()
    {
        //Enable all fields and generate serial string and add it to relevant edittext
        editTextLastName.setEnabled(true);
        editTextPhoneNo.setEnabled(true);
        editTextEmail.setEnabled(true);
        editTextFirstName.setEnabled(true);
        buttonSubmit.setEnabled(true);
        calendar = Calendar.getInstance();
        serial += calendar.get(Calendar.YEAR);
        serial += String.format("%02d",calendar.get(Calendar.MONTH));
        serial += String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH));
        serial += String.format("%02d",currentSerial);
        editTextSerialNo.getEditText().setText(serial);
        textViewSerialNo.setText("Serial no : " + serial);
        progressDialog.dismiss();
    }

    void populateSpinner()
    {
        //Populate spinners with country codes, these can be customized as necessary
        List<String> countryCode=new ArrayList<String>();
        String strTens,strUnits,strFinal;
        //below code generates 00 to 99 and adds to List countryCode.
        for(int i=0;i<10;i++)
        {
            strTens=Integer.toString(i);
            for(int j=0;j<10;j++)
            {
                strUnits=Integer.toString(j);
                strFinal=strTens.concat(strUnits);
                countryCode.add("+" + strFinal);

            }
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,countryCode);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapter);
        spinnerCountryCode.setSelection(91,true);
        spinnerCountryCode.setVisibility(View.GONE);
        spinnerCountryCode.setVisibility(View.VISIBLE);


    }

    //Functions to verify field values
    boolean verifyPhone()
    {
        String phoneNo=editTextPhoneNo.getEditText().getText().toString();
        phoneNo.trim(); //remove spaces from both ends of string
        return phoneNo.matches("[789]\\d\\d\\d\\d\\d\\d\\d\\d\\d");

    }

    boolean verifyEmail()
    {
        String email=editTextEmail.getEditText().getText().toString();
        email.trim(); //remove spaces from both ends of string
        //any combi @ any combi . any combi (.->once or zero any combi .->once or zero any combi .->once or zero any combi)->once or zero
        return email.matches("[a-z0-9A-Z_+\\.]+@[a-z0-9A-Z]+\\.[a-zA-Z0-9]+[[\\.]?[a-zA-Z0-9]+[\\.]?[a-zA-Z0-9]]?");
    }

    boolean verifyFirstName(){
        String firstName=editTextFirstName.getEditText().getText().toString();
        firstName=firstName.trim();
        return firstName.matches("[a-zA-Z]+");
    }

    boolean verifyLastName(){
        String lastName=editTextLastName.getEditText().getText().toString();
        lastName=lastName.trim();
        return lastName.matches("[a-zA-Z]+");
    }

    //Capitalize first character of string, for first name and last name fields
    String formatName(String name){
        if(name.length() > 0) {
            name = name.trim();
            name = name.toLowerCase();
            //name = name.replace(name.charAt(0), Character.toUpperCase(name.charAt(0)));
            if(name.length() > 0)
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    void clearErrors()
    {
        //Clear error of a field only if field value validated
        if(verifyEmail())
            editTextEmail.setErrorEnabled(false);
        if(verifyFirstName())
            editTextFirstName.setErrorEnabled(false);
        if(verifyLastName())
            editTextLastName.setErrorEnabled(false);

        if(verifyPhone())
            editTextPhoneNo.setErrorEnabled(false);

        editTextSerialNo.setErrorEnabled(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Form.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}

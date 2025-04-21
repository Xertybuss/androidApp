package com.example.quizapp_samsari;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Quiz4 extends AppCompatActivity {
    RadioGroup rg;
    RadioButton rb;
    RadioButton rb1, rb2, rb3, rb4;
    Button bNext, bPrevious;
    TextView questionText, questionCounter;
    ImageView quizImage;
    int score;
    String RepCorrect = "";
    String question = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz2);
        rg = findViewById(R.id.rg);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        rb4 = findViewById(R.id.rb4);
        bNext = findViewById(R.id.bNext);
        bPrevious = findViewById(R.id.bPrevious);
        questionText = findViewById(R.id.questionText);
        questionCounter = findViewById(R.id.questionCounter);
        quizImage = findViewById(R.id.quizImage);
        Intent intent=getIntent();
        score=intent.getIntExtra("score",0) ;

        // Set question counter
        questionCounter.setText("Question 2/10");
        loadQuizData();
        //Toast.makeText(getApplicationContext(),score+"",Toast.LENGTH_SHORT).show();
        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb=(RadioButton) findViewById(rg.getCheckedRadioButtonId());
                if(rg.getCheckedRadioButtonId()==-1){
                    Toast.makeText(getApplicationContext(),"Merci de choisir une réponse S.V.P !",Toast.LENGTH_SHORT).show();
                }
                else {
                    //Toast.makeText(getApplicationContext(),rb.getText().toString(),Toast.LENGTH_SHORT).show();
                    if(rb.getText().toString().equals(RepCorrect)){
                        score+=1;
                        //Toast.makeText(getApplicationContext(),score+"",Toast.LENGTH_SHORT).show();
                    }
                    Intent intent=new Intent(Quiz4.this,Quiz5.class);
                    intent.putExtra("score",score);
                    startActivity(intent);
                    //overridePendingTransition(R.anim.fadein,R.anim.fadeout);
                    overridePendingTransition(R.anim.exit,R.anim.entry);
                    finish();
                }

            }
        });
    }
    private void loadQuizData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Quiz").document("quiz4");

        // Show loading state
        questionText.setText("Loading question...");
        rb1.setEnabled(false);
        rb2.setEnabled(false);
        rb3.setEnabled(false);
        rb4.setEnabled(false);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        // Extract quiz data from the document
                        question = document.getString("question");
                        RepCorrect = document.getString("answer");

                        // Get the options
                        String option1 = document.getString("rep1");
                        String option2 = document.getString("rep2");
                        String option3 = document.getString("rep3");
                        String option4 = document.getString("rep4");

                        // Get image URL if available
                        String image = document.getString("image");

                        // Update UI with the retrieved data
                        updateQuizUI(question, option1, option2, option3, option4, image);

                        // Enable radio buttons after data is loaded
                        rb1.setEnabled(true);
                        rb2.setEnabled(true);
                        rb3.setEnabled(true);
                        rb4.setEnabled(true);


                    } else {
                        Log.d("Quiz", "Document not found in Firestore");
                        questionText.setText("Quiz data not found. Please try again later.");
                        Toast.makeText(getApplicationContext(), "Quiz data not found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("Quiz", "Error getting document: " + task.getException());
                    questionText.setText("Error loading quiz. Please check your connection.");
                    Toast.makeText(getApplicationContext(), "Error loading quiz: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateQuizUI(String question, String option1, String option2, String option3, String option4, String imageUrl) {
        // Update the question text
        questionText.setText(question);

        // Set the text for each radio button
        rb1.setText(option1);
        rb2.setText(option2);
        rb3.setText(option3);
        rb4.setText(option4);

        // Check and convert Google Drive URL
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Auto-convert Google Drive link if needed
            if (imageUrl.contains("drive.google.com")) {
                String fileId = "";

                // Extract file ID using known pattern
                if (imageUrl.contains("/file/d/")) {
                    int start = imageUrl.indexOf("/file/d/") + 8;
                    int end = imageUrl.indexOf("/", start + 1);
                    if (end == -1) end = imageUrl.length();
                    fileId = imageUrl.substring(start, end);
                }

                if (!fileId.isEmpty()) {
                    imageUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
                }
            }

            try {
                // Load the (possibly modified) image URL
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(quizImage);

            } catch (Exception e) {
                quizImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            // If no image URL is provided, use a default image
            quizImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}

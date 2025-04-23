package com.example.quizapp_samsari;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.*;

public class QuizActivity extends AppCompatActivity {

    FirebaseFirestore db;
    List<DocumentSnapshot> questionList;
    int currentIndex = 0;
    int score = 0;

    RadioGroup rg;
    RadioButton rb1, rb2, rb3, rb4;
    Button bNext, bPrevious;
    TextView questionText, questionCounter;
    ImageView quizImage;

    String correctAnswer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz1);
        Intent intent=getIntent();
        // Bind views
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

        db = FirebaseFirestore.getInstance();
        loadAllQuestions();

        bNext.setOnClickListener(view -> checkAnswerAndMove(true));
        bPrevious.setOnClickListener(view -> checkAnswerAndMove(false));
    }

    private void loadAllQuestions() {
        db.collection("Quiz").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                questionList = task.getResult().getDocuments();

                // Optional: sort documents by quiz number
                questionList.sort(Comparator.comparing(DocumentSnapshot::getId));

                showQuestion(currentIndex);
            } else {
                Toast.makeText(this, "Failed to load quiz!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showQuestion(int index) {
        if (index < 0 || index >= questionList.size()) return;

        rg.clearCheck();
        DocumentSnapshot doc = questionList.get(index);

        String question = doc.getString("question");
        String rep1 = doc.getString("rep1");
        String rep2 = doc.getString("rep2");
        String rep3 = doc.getString("rep3");
        String rep4 = doc.getString("rep4");
        correctAnswer = doc.getString("answer");
        String imageUrl = doc.getString("image");

        questionText.setText(question);
        rb1.setText(rep1);
        rb2.setText(rep2);
        rb3.setText(rep3);
        rb4.setText(rep4);
        questionCounter.setText("Question " + (index + 1) + "/" + questionList.size());

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.contains("drive.google.com")) {
                String fileId = extractGoogleDriveFileId(imageUrl);
                if (!fileId.isEmpty()) {
                    imageUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
                }
            }

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(quizImage);
        } else {
            quizImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void checkAnswerAndMove(boolean isNext) {
        int selectedId = rg.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Merci de choisir une réponse S.V.P !", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selected = findViewById(selectedId);
        String selectedAnswer = selected.getText().toString();
        if (selectedAnswer.equals(correctAnswer)) {
            score++;
        }

        if (isNext) {
            if (currentIndex < questionList.size() - 1) {
                currentIndex++;
                showQuestion(currentIndex);
            } else {
                Intent intent = new Intent(QuizActivity.this, Score.class);
                intent.putExtra("score", score);
                startActivity(intent);
            }
        } else {
            if (currentIndex > 0) {
                currentIndex--;
                showQuestion(currentIndex);
            } else {
                Toast.makeText(this, "C'est la première question", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String extractGoogleDriveFileId(String url) {
        if (url.contains("/file/d/")) {
            int start = url.indexOf("/file/d/") + 8;
            int end = url.indexOf("/", start);
            return url.substring(start, end);
        }
        return "";
    }
}

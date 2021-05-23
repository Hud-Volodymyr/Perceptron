package com.example.perceptron

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlin.math.abs
import kotlin.math.truncate

class MainActivity : AppCompatActivity() {
    private var learningSpeed = 0f
    private var timeDeadline = 0f
    private var iterations = 0
    private var threshold = 4f
    private var points = listOf(0f to 6f, 1f to 5f, 3f to 3f, 2f to 4f)
    private val illegalValues = listOf(
        Float.NaN,
        Float.NEGATIVE_INFINITY,
        Float.POSITIVE_INFINITY,
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val learningSpeedView = findViewById<EditText>(R.id.learning_speed)
        val timeDeadlineView = findViewById<EditText>(R.id.deadline)
        val iterationsView = findViewById<EditText>(R.id.iterations)
        val resultView = findViewById<TextView>(R.id.result)
        findViewById<Button>(R.id.train).setOnClickListener {
            try {
                validateInput(learningSpeedView.text.toString(), timeDeadlineView.text.toString(), iterationsView.text.toString())
                val result = train()
                resultView.text = result
            } catch (e: IllegalStateException) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInput(speed: String, deadline: String, maxIterations: String) {
        if (speed.isBlank()) {
            error("Please enter learning speed")
        }
        learningSpeed = speed.toFloat()

        if (learningSpeed !in listOf(0.001F, 0.01F, 0.05F, 0.1F, 0.2F, 0.3F)) {
            error("Learning speed must be in set of [0.001, 0.01, 0.05, 0.1, 0.2, 0.3]")
        }

        if (maxIterations.isBlank() && deadline.isBlank()) {
            error("Please enter a number of either a deadline or of iterations")
        }

        timeDeadline = if(deadline.isBlank()) 0f else deadline.toFloat()

        if (timeDeadline !in listOf(0f, 0.5F, 1F, 2F, 5F)) {
            error("Deadline must be in set of [0, 0.5, 1, 2, 5]")
        }

        timeDeadline *= 1000
        iterations = if (maxIterations.isBlank()) 0 else maxIterations.toInt()

        if (iterations == 0 && timeDeadline == 0f) {
            error("Either iterations or time deadline must be greater than 0")
        }
        if (iterations !in listOf(0, 100, 200, 500, 1000)) {
            error("Iterations must be in set of [0, 100, 200, 500, 1000]")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun train(): String {
        var w1 = 0F
        var w2 = 0F
        var i = 0
        var resultNum = 0
        val n = points.size
        val startTime = System.nanoTime()
        while (resultNum < n) {
            val time = System.nanoTime()
            if (iterations > 0) {
                if (i > iterations) return "Iterations count exceeded\nW1 = $w1, W2 = $w2\n" +
                        "Time spent: ${truncate(((time - startTime) / 1000).toDouble())}mcs"
            }
            if (timeDeadline > 0) {
                if (time - startTime > timeDeadline) {
                    return "Time deadline exceeded\nW1 = $w1, W2 = $w2 \n iterations to find result: $i\nTime spent: ${truncate(((time - startTime) / 1000).toDouble())}mcs"
                }
            }
            val currentPoint = points[i % n]
            val diff = getDiff(currentPoint, w1, w2)
            if (currentPoint.second == threshold) {
                if (abs(diff - threshold) > 0.03f) {
                    val d = threshold - diff
                    w1 += d * currentPoint.first * learningSpeed
                    w2 += d * currentPoint.second * learningSpeed
                    resultNum = 0
                    if (w1 in illegalValues || w2 in illegalValues) {
                        error("Solution could not be found\n"
                                + "Number of iterations: $i\n" +
                                "Time spent: ${truncate(((time - startTime) / 1000).toDouble())}mcs")
                    }
                } else {
                    ++resultNum
                }
            } else if (currentPoint.second < threshold) {
                if (diff > threshold) {
                    val d = threshold - diff
                    w1 += d * currentPoint.first * learningSpeed
                    w2 += d * currentPoint.second * learningSpeed
                    resultNum = 0
                    if (w1 in illegalValues || w2 in illegalValues) {
                        error("Solution could not be found\n" +
                                "Number of iterations: $i\n" +
                                "Time spent: ${truncate(((time - startTime) / 1000).toDouble())}mcs")
                    }
                } else {
                    ++resultNum
                }
            } else {
                if (diff < threshold) {
                    val d = threshold - diff
                    w1 += d * currentPoint.first * learningSpeed
                    w2 += d * currentPoint.second * learningSpeed
                    resultNum = 0
                    if (w1 in illegalValues || w2 in illegalValues) {
                        error("Solution could not be found"
                                + "Number of iterations: $i\n" +
                                "Time spent: ${truncate(((time - startTime) / 1000).toDouble())}mcs")
                    }
                } else {
                    ++resultNum
                }
            }
            ++i
        }
        return "Successfully completed the training\nW1 = $w1, W2 = $w2 \n iterations to find result: $i\n" +
                "Time spent: ${truncate(((System.nanoTime() - startTime) / 1000).toDouble())}mcs"
    }

    private fun getDiff(point: Pair<Float, Float>, w1: Float, w2: Float): Float {
        return point.first * w1 + point.second * w2
    }
}
package org.wbftw.weil.sos_flashlight.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.wbftw.weil.sos_flashlight.utils.MorseCodeUtils
import org.wbftw.weil.sos_flashlight.R
import org.wbftw.weil.sos_flashlight.SOSFlashlightApp
import org.wbftw.weil.sos_flashlight.databinding.FragmentSecondBinding
import org.wbftw.weil.sos_flashlight.ui.viewmodel.SecondViewModel
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * [CodeReaderFragment] is used to handle Morse code input and display the corresponding text.
 */
class CodeReaderFragment : Fragment() {

    val TAG = "SecondFragment"

    private var _binding: FragmentSecondBinding? = null
    private val viewModel: SecondViewModel by lazy {
        ViewModelProvider(this).get(SecondViewModel::class.java)
    }
    private var lastBeepStartTime: Long = -1L
    private var queueCode: MutableList<Char> = mutableListOf()
    private var spaceAdded: Boolean = false
    private var wordSpaceAdded: Boolean = false

    private var mTimeExecutorService: ScheduledExecutorService? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireContext().applicationContext as SOSFlashlightApp

        viewModel.interval.value = app.defaultIntervalShortMs

        viewModel.textCode.observe(viewLifecycleOwner) { code ->
            binding.editTextCode.setText(code)
            //scroll to the end of the EditText
            binding.editTextCode.setSelection(code.length)
        }

        viewModel.textClear.observe(viewLifecycleOwner) { clearText ->
            binding.editTextClear.setText(clearText)
            //scroll to the end of the EditText
            binding.editTextClear.setSelection(clearText.length)
        }

        binding.buttonClick.setOnClickListener {
            findNavController().navigate(R.id.FirstFragment)
        }

        binding.buttonClear.setOnClickListener {
            viewModel.textCode.value = ""
            viewModel.textClear.value = ""
            queueCode = mutableListOf()
            spaceAdded = false
            wordSpaceAdded = false
            lastBeepStartTime = -1L
            stopCounter()
        }

        binding.buttonClickDot.setOnClickListener {
            // Handle button click for dot
            viewModel.addMorseCode('.')
            queueCode.add('.')
            spaceAdded = false
            wordSpaceAdded = false
            lastBeepStartTime = System.currentTimeMillis()
            startCounter()
        }

        binding.buttonClickDash.setOnClickListener {
            // Handle button click for dash
            viewModel.addMorseCode('-')
            queueCode.add('-')
            spaceAdded = false
            wordSpaceAdded = false
            lastBeepStartTime = System.currentTimeMillis()
            startCounter()
        }

        binding.buttonClick.setOnClickListener {
            // Handle space button click
            if (queueCode.isNotEmpty()) {
                handleSpace()
                startCounter()
            } else {
                Log.d(TAG, "Queue is empty, no character to process.")
            }
        }

        binding.editTextCode.setOnEditorActionListener { _, actionId, _ ->
            Log.v(TAG, "Editor action: $actionId")
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                queueCode.clear()
                spaceAdded = false
                wordSpaceAdded = false
                stopCounter()
                val code = binding.editTextCode.text.toString()
                viewModel.textClear.value = MorseCodeUtils.Companion.findSentenceInMorseCode(code)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false

        }

    }

    private fun handleSpace(){
        Log.d(TAG, "Processing space with queueCode: $queueCode")
        val charArray = queueCode.toCharArray()
        val morseChar = MorseCodeUtils.Companion.findCharacterInMorseCode(charArray)
        if (morseChar!=null){
            queueCode.clear()
            viewModel.addClearText(morseChar)
            viewModel.addMorseCode(' ')
            lastBeepStartTime = System.currentTimeMillis()
            spaceAdded = true
        }else{
            Log.d(TAG, "No character found for Morse code: ${String(charArray)}")
        }
        Log.d(TAG, "Processed character: $morseChar")
        wordSpaceAdded = false
    }

    private fun startCounter(){
        stopCounter()
        val shortTime = viewModel.interval.value ?: 250L
        mTimeExecutorService = Executors.newSingleThreadScheduledExecutor()
        mTimeExecutorService?.scheduleWithFixedDelay({
            val lastTime = System.currentTimeMillis() - lastBeepStartTime
            val lastCharacter = queueCode.lastOrNull()
            if (lastCharacter!=null && queueCode.isNotEmpty() && !spaceAdded && !wordSpaceAdded) {
                if ((lastCharacter=='.' && lastTime >= shortTime * 4 && lastTime <= shortTime * 4.5) ||
                    (lastCharacter=='-' && lastTime >= shortTime * 6 && lastTime <= shortTime * 6.5)) {
                    // treat it as a space
                    handleSpace()
                }
            }else if ((lastTime >= shortTime * 7) && (lastTime <= shortTime * 7.5) && !wordSpaceAdded) {
                // If the time since the last signal is greater than 6.5 times the interval, treat it as a word space
                Log.d(TAG, "Processing word space with queueCode: $queueCode")
                queueCode.clear()
                viewModel.addClearText(' ')
                viewModel.addMorseCode('/')
                lastBeepStartTime = System.currentTimeMillis()
                wordSpaceAdded = true
                spaceAdded = false
                stopCounter()
            }else if(lastTime > shortTime * 7){
                stopCounter()
            }
        }, 0, shortTime, TimeUnit.MILLISECONDS)
        Log.d(TAG, "Counter started with interval: $shortTime ms")
    }

    private fun stopCounter() {
        mTimeExecutorService?.shutdownNow()
        Log.d(TAG, "Counter stopped")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
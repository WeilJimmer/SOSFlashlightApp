package org.wbftw.weil.sos_flashlight

import android.util.Log

class MorseCodeUtils {

    companion object {

        val TAG = "MorseCodeUtils"
        val CODE_TABLE: Map<Char, String> = mapOf(
            'A' to ".-",
            'B' to "-...",
            'C' to "-.-.",
            'D' to "-..",
            'E' to ".",
            'F' to "..-.",
            'G' to "--.",
            'H' to "....",
            'I' to "..",
            'J' to ".---",
            'K' to "-.-",
            'L' to ".-..",
            'M' to "--",
            'N' to "-.",
            'O' to "---",
            'P' to ".--.",
            'Q' to "--.-",
            'R' to ".-.",
            'S' to "...",
            'T' to "-",
            'U' to "..-",
            'V' to "...-",
            'W' to ".--",
            'X' to "-..-",
            'Y' to "-.--",
            'Z' to "--..",
            '0' to "-----",
            '1' to ".----",
            '2' to "..---",
            '3' to "...--",
            '4' to "....-",
            '5' to ".....",
            '6' to "-....",
            '7' to "--...",
            '8' to "---..",
            '9' to "----.",
            '.' to ".-.-.-", // period
            ':' to "---...", // colon
            ',' to "--..--", // comma
            ';' to "-.-.-.", // semicolon
            '?' to "..--..", // question mark
            '=' to "-...-", // equals sign'
            '\'' to ".----.", // apostrophe`
            '/' to "-..-.", // slash
            '!' to "-.-.--", // exclamation mark
            '-' to "-....-", // hyphen
            '_' to "..--.-", // underscore
            '"' to ".-..-.", // double quote
            '(' to "-.--.", // left parenthesis
            ')' to "-.--.-", // right parenthesis
            '$' to "...-..-", // dollar sign
            '&' to ".-...", // ampersand
            '@' to ".--.-.", // at sign
            '+' to ".-.-.", // plus sign
            ' ' to "/", // space character for word separation
        )

        /**
         * Encodes a given string to Morse code.
         * @param input The string to encode.
         * @return The Morse code representation of the input string.
         * @sample input = "SOS" returns "... --- .../", where "/" indicates word space.
         */
        fun encodeWordToMorseCode(input: String): String {
            return input.uppercase().map { CODE_TABLE[it] ?: "" }.joinToString(" ").plus("/") // add word space at the end
        }

        fun findCharacterInMorseCode(morseCode: CharArray): Char? {
            Log.d(TAG, "Finding character for Morse code: ${String(morseCode)}")
            for ((char, code) in CODE_TABLE) {
                if (code == String(morseCode)) {
                    Log.d(TAG, "Found character: $char for Morse code: ${String(morseCode)}")
                    return char
                }
            }
            Log.d(TAG, "No character found for Morse code: ${String(morseCode)}")
            return null // return null if not found
        }

        /**
         * Finds a sentence in Morse code.
         * @param morseCode The Morse code string to decode.
         * @return The decoded sentence as a string.
         * @sample morseCode = "... --- ... /... --- ..." returns "SOS SOS"
         */
        fun findSentenceInMorseCode(morseCode: String): String {
            Log.d(TAG, "Finding sentence for Morse code: $morseCode")
            val words = morseCode.split("/") // split by word space
            val sentence = StringBuilder()
            for (word in words) {
                val chars = word.split(" ")
                for (char in chars) {
                    if (char.isEmpty()){
                        continue
                    }
                    val foundChar = findCharacterInMorseCode(char.toCharArray())
                    if (foundChar != null) {
                        sentence.append(foundChar)
                    }else{
                        sentence.append('？') // append '？' for unknown characters
                    }
                }
                sentence.append(' ') // add space between words
            }
            Log.d(TAG, "Found sentence: $sentence")
            return sentence.toString().trim() // trim to remove trailing space
        }

        fun runMoseCode(moseCode: String, dot: () -> Unit, dash: () -> Unit, space: () -> Unit, wordSpace: () -> Unit, stopFlag: () -> Boolean) {
            val parts = moseCode.toCharArray()
            for (part in parts) {
                Log.d(TAG, "Processing part: $part")
                when (part) {
                    '.' -> dot()
                    '-' -> dash()
                    ' ' -> space()
                    else -> wordSpace() // handle word space
                }
                if (stopFlag()) {
                    Log.d(TAG, "Stop flag is set, exiting Morse code processing.")
                    break // stop if the stop flag is set
                }
            }
        }

    }

}
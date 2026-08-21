package com.example.cyloop.crypto

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * A minimal Solana Transaction builder for standard SOL transfers.
 * Avoids heavy SDK dependencies by implementing the binary layout manually.
 * Compatible with Solana Legacy Message format.
 */
object SolanaTransaction {

    @OptIn(ExperimentalEncodingApi::class)
    fun createTransferTransaction(
        senderAddress: String,
        receiverAddress: String,
        lamports: Long,
        recentBlockhash: String,
        privateKey: ByteArray // 64 bytes (seed + pubkey) or 32 bytes (seed)
    ): String {
        val senderPubkey = CryptoUtils.decodeBase58(senderAddress)
        val receiverPubkey = CryptoUtils.decodeBase58(receiverAddress)
        val blockhash = CryptoUtils.decodeBase58(recentBlockhash)

        // Instruction Data for SystemProgram Transfer:
        // [2, 0, 0, 0] (32-bit LE) + lamports (64-bit LE)
        val instructionData = ByteArray(12)
        instructionData[0] = 2 // SystemProgram.Transfer index
        for (i in 0..7) {
            instructionData[4 + i] = ((lamports shr (i * 8)) and 0xFF).toByte()
        }

        // --- TRANSACTION MESSAGE (Legacy) ---
        // 1. Header (3 bytes)
        // numRequiredSignatures: 1 (sender)
        // numReadOnlySignedAccounts: 0
        // numReadOnlyUnsignedAccounts: 1 (System Program)
        val header = byteArrayOf(1, 0, 1)

        // 2. Account Addresses (compact-u16 size + addresses)
        // Order must be: [Signed, Writable] -> [Unsigned, Writable] -> [Unsigned, Read-only]
        // [Sender (S,W), Receiver (U,W), SystemProgram (U,R)]
        val systemProgramId = ByteArray(32) // System Program is all zeros
        val accountsCount = byteArrayOf(3)
        val accounts = senderPubkey + receiverPubkey + systemProgramId

        // 3. Recent Blockhash (32 bytes)
        // (already decoded above)

        // 4. Instructions (compact-u16 size + instructions)
        // Each instruction: [programIdIndex, accountsCount, accounts, dataLength, data]
        val instruction = byteArrayOf(
            2, // programIdIndex: index of System Program (index 2 in accounts array)
            2, // accountsCount: 2 accounts passed to instruction (from, to)
            0, // account index 0: sender
            1, // account index 1: receiver
            12 // dataLength: 12 bytes
        ) + instructionData

        val instructionList = byteArrayOf(1) + instruction // 1 instruction

        // Build the Message
        val message = header + accountsCount + accounts + blockhash + instructionList

        // --- SIGNING ---
        // Sign the message with Ed25519. We use the 32-byte seed.
        val seed = if (privateKey.size == 64) privateKey.copyOfRange(0, 32) else privateKey
        val signature = Ed25519.sign(message, seed)

        // --- FINAL TRANSACTION WIRE FORMAT ---
        // [Signatures Count (compact-u16), Signature 1, ..., Message]
        val transaction = byteArrayOf(1) + signature + message

        // Encode to Base64 (Standard)
        return Base64.encode(transaction)
    }
}

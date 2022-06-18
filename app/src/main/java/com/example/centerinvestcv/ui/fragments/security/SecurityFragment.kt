package com.example.centerinvestcv.ui.fragments.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.SecurityFragmentBinding
import com.example.centerinvestcv.utils.FullScreenStateChanger

class SecurityFragment : Fragment() {

    private var _binding: SecurityFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SecurityFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            backButton.setOnClickListener {
                findNavController().popBackStack()
            }
            hidingData.setOnClickListener {
                findNavController().navigate(R.id.action_securityFragment_to_faceManagerFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, true)
    }

    override fun onStop() {
        super.onStop()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
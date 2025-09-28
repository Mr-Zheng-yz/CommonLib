package com.baize.common_lib.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.baize.common_lib.databinding.FragmentHomeBinding
import com.baize.common_lib.utils.AudioUtil
import com.baize.common_lib.utils.PlayerUtil
import com.ocamara.common_libs.mulit_toast.removePersistentRedToast
import com.ocamara.common_libs.mulit_toast.showBlueToast
import com.ocamara.common_libs.mulit_toast.showGreenToast
import com.ocamara.common_libs.mulit_toast.showPersistentRedToast
import com.ocamara.common_libs.mulit_toast.showRedToast
import com.ocamara.common_libs.utils.LogUtil

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var playerUtil: PlayerUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        playerUtil = PlayerUtil(requireContext())
        playerUtil.setListener(object: PlayerUtil.OnPlaybackListener {
            override fun onPreparing() {
                LogUtil.d("PlayerUtil", "onPreparing")
            }

            override fun onPlaying() {
                LogUtil.d("PlayerUtil", "onPlaying")
            }

            override fun onCompleted() {
                LogUtil.d("PlayerUtil", "onCompleted")
            }

            override fun onError(error: String?) {
                LogUtil.d("PlayerUtil", "onError:$error")
            }
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var playMp3Listener = View.OnClickListener { v ->
            var playUrl = binding.etPlayUrl.text.toString()
            playerUtil.playAudioFromUrl(playUrl, v == binding.btnSpeaker)
        }
        binding.btnSpeaker.setOnClickListener(playMp3Listener)
        binding.btnHeadset.setOnClickListener(playMp3Listener)

        val seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (seekBar === binding.pbVolumeMedia) {
                    AudioUtil.getInstance().setMediaVolume(i)
                    LogUtil.d("Volume", "--设置媒体音量:$i")
                } else if (seekBar === binding.pbVolumeCall) {
                    AudioUtil.getInstance().setCallVolume(i)
                    LogUtil.d("Volume", "--设置通话音量:$i")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (seekBar === binding.pbVolumeMedia) {
                    binding.pbVolumeMedia.setProgress(AudioUtil.getInstance().getMediaVolume())
                    LogUtil.d(
                        "Volume",
                        "--当前媒体音量:" + AudioUtil.getInstance().getMediaVolume()
                    )
                } else if (seekBar === binding.pbVolumeCall) {
                    binding.pbVolumeCall.setProgress(AudioUtil.getInstance().getCallVolume())
                    LogUtil.d(
                        "Volume",
                        "--当前通话音量:" + AudioUtil.getInstance().getCallVolume()
                    )
                }
            }
        }
        binding.pbVolumeMedia.setMax(AudioUtil.getInstance().mediaMaxVolume)
        binding.pbVolumeMedia.setProgress(AudioUtil.getInstance().mediaVolume)
        binding.pbVolumeMedia.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.pbVolumeCall.setMax(AudioUtil.getInstance().callMaxVolume)
        binding.pbVolumeCall.setProgress(AudioUtil.getInstance().callVolume)
        binding.pbVolumeCall.setOnSeekBarChangeListener(seekBarChangeListener)

        binding.btnToast1.setOnClickListener {
            requireActivity().showRedToast("人生得意须尽欢，莫使金樽空对月")
        }
        binding.btnToast2.setOnClickListener {
            requireActivity().showGreenToast("世间行乐亦如此，古来万事东流水，别君去时何时还，且放白鹿青涯间，需行即骑访名山，安能摧眉折腰事权贵，使我不得开心颜")
        }
        binding.btnToast3.setOnClickListener {
            requireActivity().showBlueToast("诗酒趁年华")
        }
        binding.btnToast4.setOnClickListener {
            requireActivity().showPersistentRedToast("五花马，千金裘，呼儿将出换美酒，与尔同销万古愁", "将进酒")
        }
        binding.btnToast5.setOnClickListener {
            requireActivity().removePersistentRedToast("将进酒")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
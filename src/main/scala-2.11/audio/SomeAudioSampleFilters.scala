package audio

/**
  * Created by tkuczma on 01.12.15.
  */
object DoNothingAudioSampleFilters extends Function1[AudioSample, AudioSample] {
  def apply(data: AudioSample) = data
}


object HighLowCutAudioSampleFilters extends Function1[AudioSample, AudioSample] {
  def apply(data: AudioSample) = {
    val filteredData = data.groupedToInt map (_.min(0xf1aa).max(0xff))
    new AudioSample(filteredData, data.format)
  }
}

/**
  * Based on https://ccrma.stanford.edu/~jos/fp/Definition_Simplest_Low_Pass.html
  */
object LowPassAudioSampleFilters extends Function1[AudioSample, AudioSample] {
  def apply(data: AudioSample) = {
    val filteredData = data.groupedToInt.sliding(2).map(_.sum).toArray
    new AudioSample(filteredData, data.format)
  }
}

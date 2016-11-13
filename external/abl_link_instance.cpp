/*
 *  For information on usage and redistribution, and for a DISCLAIMER OF ALL
 *  WARRANTIES, see the file, LICENSE, in the root of this repository.
 *
 */

#include "abl_link_instance.hpp"

#include "s_stuff.h"  // Only for DEFDACBLKSIZE.

namespace abl_link {

std::weak_ptr<AblLinkWrapper> AblLinkWrapper::shared_instance;

AblLinkWrapper::AblLinkWrapper(double bpm) :
    link(bpm),
    timeline(ableton::link::Timeline(), false),
    time_filter(
        ableton::link::HostTimeFilter<ableton::link::platform::Clock>()),
    num_peers_sym(gensym("#abl_link_num_peers")),
    num_peers(-1),
    sample_time(0.0),
    invocation_count(0) {
  post("Created new Link instance with tempo %f.", bpm);
}

void AblLinkWrapper::enable(bool enabled) { link.enable(enabled); }

ableton::Link::Timeline& AblLinkWrapper::acquireAudioTimeline(
    std::chrono::microseconds *current_time, double advance_ms) {
  if (invocation_count++ == 0) {
    const int n = link.numPeers();
    if (n != num_peers && num_peers_sym->s_thing) {
      pd_float(num_peers_sym->s_thing, n);
      num_peers = n;
    }
    timeline = link.captureAudioTimeline();
    sample_time += DEFDACBLKSIZE;
    curr_time = time_filter.sampleTimeToHostTime(sample_time) + 
      std::chrono::milliseconds((long)advance_ms);
  }
  *current_time = curr_time;
  return timeline;
}

void AblLinkWrapper::releaseAudioTimeline() {
  if (invocation_count >= shared_instance.use_count()) {
    link.commitAudioTimeline(timeline);
    invocation_count = 0;
  }
}

std::shared_ptr<AblLinkWrapper>
    AblLinkWrapper::getSharedInstance(double bpm) {
  auto ptr = shared_instance.lock();
  if (!ptr) {
    ptr.reset(new AblLinkWrapper(bpm));
    shared_instance = ptr;
  } else {
    post("Using existing Link instance with ref count %d.", ptr.use_count());
  }
  return ptr;
}

}  // namespace abl_link

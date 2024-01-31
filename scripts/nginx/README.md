This is a file about how to deploy nginx for the backend.
There are two main reasons for using this framework.
1. HLS video streaming.
    HLS Video Streaming just need nginx as a proxy who is used to validate the authority of the video.
2. 
    nginx-hls plugin would help stream videos on demand. It is the best tool to meet the on demand video requirements. 
    Since a single video is split into several pieces.
    And SRS help us handle a potential need of live.
    
3. Load balancing.
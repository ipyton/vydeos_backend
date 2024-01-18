This is a file about how to deploy nginx for the backend.
There are two main reasons for using this framework.
1. HLS video streaming.
    nginx-hls plugin would help us stream videos on demand. It is the best tool to meet the on demand video requirements. 
    Since a single video is split into several pieces.
    And SRS help us handle a potential need of live.
    
2. Load balancing.
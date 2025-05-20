package com.chen.blogbackend.util;

import com.chen.blogbackend.entities.UnfinishedUpload;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.*;
import java.util.ArrayList;

public class VideoUtil {

    static String ffmpegPath = "H:\\ffmpeg-master-latest-win64-gpl\\bin\\ffmpeg.exe";
    static String ffprobePath = "H:\\ffmpeg-master-latest-win64-gpl\\bin\\ffprobe.exe";
    public static String sourceFilePath = "./videos";


    public static void concat(UnfinishedUpload upload) throws Exception {
        File file = new File(upload.getOwnerName());
        FileWriter writer = new FileWriter(file);
        char[] cs = new char[1024];
        for (int i = 0; i < upload.getTotal(); i ++ ) {
            File fileSlice = new File(upload.getFileHash() + "_" + i);
            FileReader reader = new FileReader(fileSlice);
            if (fileSlice.length() == 0) {
                throw new Exception("file length is 0");
            }
            int read;
            while((read = reader.read(cs)) > 0 ) {
                writer.write(cs, 0, read);
            }
            reader.close();
        }
        writer.close();
    }


    public static void convertToHLS(UnfinishedUpload upload) throws IOException, InterruptedException {
//        FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
//        FFprobe ffprobe = new FFprobe(ffprobePath);
//        FFmpegBuilder builder = new FFmpegBuilder().setInput("G:\\11.2VIP_ev.mp4").overrideOutputFiles(true)
//                .addOutput("G:\\11.2VIP_ev.mp4").setFormat("m3u8").done()
//         ffmpeg -i input.mp4 -c:v copy -force_key_frames "expr:gte(t,n_forced*2)" -hls_time 2  -hls_segment_filename %d.ts -f hls output/playlist.m3u8
//
        // ArrayList<String> arr = new ArrayList<>();
//        arr.add("ffmpeg");
//        arr.add(" -i");
//        arr.add(" G:\\m.mp4");
//        arr.add(" -c:v copy");
//        arr.add(" -force_key_frames \"expr:gte(t,n_forced*2)\" -hls_time 2 ");
//        arr.add(" -hls_segment_filename %d.ts -f hls G:\\output\\playlist.m3u8");
        //ProcessBuilder builder = new ProcessBuilder("F:\\blog\\ffmpeg_commands.cmd");

//        Process start = builder.start();
//        InputStream errorStream = start.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
//        while (reader.ready()) {
//            System.out.println(reader.readLine());
//        }
//        int i = start.waitFor();
//        System.out.println(i);
        //boolean mkdirs = new File("sourcePath" + upload.getFileHash() + "_" +"encoded").mkdirs();


        Process exec = Runtime.getRuntime().exec("H:\\ffmpeg-master-latest-win64-gpl\\bin\\ffmpeg.exe -i G:\\m.mp4 -c:v copy G:\\output\\a.m3u8 -hls_segment_filename %d.ts -f hls");
        InputStream inputStream = exec.getErrorStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        while (true) {
            String s = bufferedReader.readLine();
            if (s == null) {
                break;
            }
            System.out.println(s);
        }
        exec.waitFor();
    }



    public static void main(String[] args) throws IOException, InterruptedException {
        convertToHLS(null);
    }
}

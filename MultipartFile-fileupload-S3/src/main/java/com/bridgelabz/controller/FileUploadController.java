package com.bridgelabz.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;

import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Controller
public class FileUploadController {
	private String S3_BUCKET_NAME = "your bucket name";

	private AmazonS3 s3Client = new AmazonS3Client(new BasicAWSCredentials("your AK", "your FK"));

	@GetMapping("/")
	public String fileUploadForm(Model model) {
		return "fileUploadForm";
	}

	// Handling multiple files upload request
	@PostMapping("/multipleFileUpload")
	public String multipleFileUpload(@RequestParam("file") MultipartFile[] files, Model model) throws IOException {

		// Save file on system
		for (MultipartFile file : files) {
			if (!file.getOriginalFilename().isEmpty()) {
				BufferedOutputStream outputStream = new BufferedOutputStream(
						new FileOutputStream(new File("/home/bridgeit/test", file.getOriginalFilename())));

				outputStream.write(file.getBytes());
				outputStream.flush();
				outputStream.close();
			} else {
				model.addAttribute("msg", "Please select at least one file..");
				return "fileUploadForm";
			}
		}
		model.addAttribute("msg", "Multiple files uploaded successfully.");
		return "fileUploadForm";
	}

	@RequestMapping("/singleFileUpload")
	public String singleFileUpload(@RequestParam("file") MultipartFile file, Model model) throws IOException {

		// Save file on system
		if (!file.getOriginalFilename().isEmpty()) {
			System.out.println(file.getOriginalFilename());
			BufferedOutputStream outputStream = new BufferedOutputStream(
					new FileOutputStream(new File("/home/bridgeit/test", file.getOriginalFilename())));
			String location = "/home/bridgeit/test" + file.getOriginalFilename();
			System.out.println("File location ::" + location);

			// other method to ram usage reducation..

			/*
			 * InputStream is = file.getInputStream(); byte[] ba = new
			 * byte[8192]; while (is.available()) { byte b = is.read(ba);
			 * outputStream.write(ba); }
			 */
			outputStream.write(file.getBytes());
			outputStream.flush();
			outputStream.close();
			saveFileToS3(file);
			
			model.addAttribute("msg", "File uploaded successfully.");
		} else {
			model.addAttribute("msg", "Please select a valid file..");
		}
		return "fileUploadForm";
	}

	/**
	 * 
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 *             <p>
	 *             saveFileToS3 accepts MultipartFile returns s3 file location
	 */
	public CustomerImage saveFileToS3(MultipartFile multipartFile) throws IOException {
		File fileToUpload = convertFromMultiPart(multipartFile);
		// used to record event time-stamps in the application.
		String key = Instant.now().getEpochSecond() + "_" + fileToUpload.getName();
		/* save file in bucket */
		s3Client.putObject(new PutObjectRequest(S3_BUCKET_NAME, key, fileToUpload));
		/* get signed URL (valid for one year) */
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(S3_BUCKET_NAME, key);
		generatePresignedUrlRequest.setMethod(HttpMethod.GET);
		generatePresignedUrlRequest.setExpiration(DateTime.now().plusYears(1).toDate());
		URL signedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
		//return location of file stored
		return new CustomerImage(key, signedUrl.toString());
	}

	private File convertFromMultiPart(MultipartFile multipartFile) throws IOException {

		File file = new File(multipartFile.getOriginalFilename());
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(multipartFile.getBytes());
		fos.close();

		return file;
	}

	public void deleteImageFromS3(CustomerImage customerImage) {
		s3Client.deleteObject(new DeleteObjectRequest(S3_BUCKET_NAME, customerImage.getKey()));
	}

}

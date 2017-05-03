#include <lukeentertainment_example_OpencvNativeClass.h>
const int MIN_CONTOUR_AREA = 30;

const int RESIZED_IMAGE_WIDTH = 20;
const int RESIZED_IMAGE_HEIGHT = 30;
std::string str="";
std::string strFinalString="";
    int checkBounds(Rect,Rect);
  class ContourWithData {
  public:
      // member variables ///////////////////////////////////////////////////////////////////////////
      std::vector<cv::Point> ptContour;           // contour
      cv::Rect boundingRect;                      // bounding rect for contour
      float fltArea;                              // area of contour

      ///////////////////////////////////////////////////////////////////////////////////////////////
      bool checkIfContourIsValid() {                              // obviously in a production grade program
          if (fltArea < MIN_CONTOUR_AREA) return false;           // we would have a much more robust function for
          return true;                                            // identifying if a contour is valid !!
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////
      static bool sortByBoundingRectXPosition(const ContourWithData& cwdLeft, const ContourWithData& cwdRight) {      // this function allows us to sort
          return(cwdLeft.boundingRect.x > cwdRight.boundingRect.x);                                                   // the contours from left to right
      }
      static bool sortByBoundingRectYPosition(const ContourWithData& cwdTop, const ContourWithData& cwdBot) {      // this function allows us to sort
           return(cwdTop.boundingRect.y > cwdBot.boundingRect.y);                                                   // the contours from left to right
      }

  };


JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_train
  (JNIEnv *env, jclass, jlong addrRgba,jstring path,jint option){
  cv::Mat& mRgb=*(cv::Mat*)addrRgba;

  const jsize len = env->GetStringUTFLength(path);
  const char* strChars = env->GetStringUTFChars(path, (jboolean *)0);
  std::string Result(strChars, len);
  char classPath[100],imagePath[100];

  strcpy(classPath,strChars);
  strcpy(imagePath,strChars);

  env->ReleaseStringUTFChars(path, strChars);
  strcat(classPath,"classifications.xml");
  strcat(imagePath,"images.xml");
  __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG"," %s\n",classPath);

  std::vector<ContourWithData> allContoursWithData;           // declare empty vectors,
  std::vector<ContourWithData> validContoursWithData;

  cv::Mat imgTrainingNumbers;         // input image
  cv::Mat imgGrayscale;               //
  cv::Mat imgBlurred;                 // declare various images
  cv::Mat imgThresh;                  //
  cv::Mat imgThreshCopy;
  cv::Mat imgCanny;

  std:: vector<std::vector<cv::Point> > ptContours;        // declare contours vector
  std::vector<cv::Vec4i> v4iHierarchy;                    // declare contours hierarchy

  cv::Mat matClassificationInts;      // these are our training classifications, note we will have to perform some conversions before writing to file later

                                    // these are our training images, due to the data types that the KNN object KNearest requires, we have to declare a single Mat,
                                    // then append to it as though it's a vector, also we will have to perform some conversions before writing to file later
  cv::Mat matTrainingImages;
  imgTrainingNumbers =(cv::Mat) mRgb;
  if (imgTrainingNumbers.empty()) {                               // if unable to open image
      __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error: image not read from file\n\n");         // show error message on command line
            return((jint)0);                                                  // and exit program
  }
  cv::cvtColor(imgTrainingNumbers, imgGrayscale, CV_RGB2GRAY);        // convert to grayscale

  //cv::GaussianBlur(imgGrayscale,imgBlurred,cv::Size(5,5),0);
  cv::threshold(imgGrayscale,imgThresh, 0, 255, CV_THRESH_BINARY_INV | CV_THRESH_OTSU);                           // constant subtracted from the mean or weighted mean

  imgThreshCopy = imgThresh.clone();
  cv::findContours(imgThreshCopy,ptContours,v4iHierarchy,cv::RETR_EXTERNAL,cv::CHAIN_APPROX_SIMPLE);

  __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG"," length: %d\n",ptContours.size());
  for (int i = 0; i < ptContours.size(); i++) {               // for each contour
                ContourWithData contourWithData;                                                    // instantiate a contour with data object
                contourWithData.ptContour = ptContours[i];                                          // assign contour to contour with data
                contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);         // get the bounding rect
                contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);               // calculate the contour area
                if(contourWithData.fltArea >MIN_CONTOUR_AREA)
                     validContoursWithData.push_back(contourWithData);                                   // add contour with data object to list of all contours with data
  }


  if(option==1)
  {
            cv::FileStorage fsClassifications(classPath, cv::FileStorage::READ);        // open the classifications file
            fsClassifications["classifications"] >> matClassificationInts;      // read classifications section into Mat classifications variable
            fsClassifications.release();

            cv::FileStorage fsTrainingImages(imagePath, cv::FileStorage::READ);          // open the training images file
            fsTrainingImages["images"] >> matTrainingImages;           // read images section into Mat training images variable
            fsTrainingImages.release();
  }
  for (int i = 0; i < validContoursWithData.size(); i++) {
            // for each contour
            if (true) {                // if contour is big enough to consider
                cv::Rect boundingRect = validContoursWithData[i].boundingRect;                // get the bounding rect
                cv::rectangle(imgTrainingNumbers, boundingRect, cv::Scalar(255,0,0), 2);      // draw red rectangle around each contour as we ask user for input

                cv::Mat matROI = imgThresh(boundingRect);           // get ROI image of bounding rect
                cv::Mat matROIResized;
                cv::resize(matROI, matROIResized, cv::Size(RESIZED_IMAGE_WIDTH, RESIZED_IMAGE_HEIGHT));     // resize image, this will be more consistent for recognition and storage
                int intChar=0;
                if(i>=10&&i<36)
                {
                    intChar=(i+7)+'0';
                }
                else if(i>=36)
                {
                    intChar=(i+7+6)+'0';
                }
                else
                    intChar=i+'0';
                if (true) {     // else if the char is in the list of chars we are looking for . . .

                    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG"," char : %c\n",char(intChar));
                    matClassificationInts.push_back(intChar);       // append classification char to integer list of chars

                    cv::Mat matImageFloat;                          // now add the training image (some conversion is necessary first) . . .
                    matROIResized.convertTo(matImageFloat, CV_32FC1);       // convert Mat to float

                    cv::Mat matImageReshaped = matImageFloat.reshape(1, 1);       // flatten

                    matTrainingImages.push_back(matImageReshaped);
                           // add to Mat as though it was a vector, this is necessary due to the
                                                                                                // data types that KNearest.train accepts
                }   // end if
            }   // end if
  }   // end for

  __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG", "\n trainging complete \n");


                    // save classifications to file ///////////////////////////////////////////////////////
        cv::Mat matClassificationFloats;
        matClassificationInts.convertTo(matClassificationFloats,CV_32FC1);
        cv::FileStorage fsClassifications(classPath, cv::FileStorage::WRITE);           // open the classifications file

        if (fsClassifications.isOpened() == false) {                                                        // if the file was not opened successfully
            __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error, unable to open training classifications file, exiting program1\n\n");        // show error message
            return(0);                                                                                      // and exit program
        }

        fsClassifications << "classifications" <<matClassificationInts;        // write classifications into classifications section of classifications file
        fsClassifications.release();                                            // close the classifications file

                    // save training images to file ///////////////////////////////////////////////////////

        cv::FileStorage fsTrainingImages(imagePath, cv::FileStorage::WRITE);         // open the training images file

        if (fsTrainingImages.isOpened() == false) {                                                 // if the file was not opened successfully
            __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error, unable to open training images file, exiting program\n\n");         // show error message
            return(0);                                                                              // and exit program
        }

        fsTrainingImages << "images" << matTrainingImages;         // write training images into images section of images file
    fsTrainingImages.release();
    mRgb=imgTrainingNumbers;
    return (jint)1;
  }


  JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_testInput
    (JNIEnv *env, jclass,jlong addrRgba,jstring path){

    cv::Mat& mRgb=*(cv::Mat*)addrRgba;

    const jsize len = env->GetStringUTFLength(path);
       const char* strChars = env->GetStringUTFChars(path, (jboolean *)0);
       std::string Result(strChars, len);
       char classPath[100],imagePath[100],dataPath[100];

       strcpy(classPath,strChars);
       strcpy(imagePath,strChars);
       strcpy(dataPath,strChars);

       env->ReleaseStringUTFChars(path, strChars);
        strcat(classPath,"classifications.xml");
        strcat(imagePath,"images.xml");
        strcat(dataPath,"data.txt");

    std::vector<ContourWithData> validContoursWithData,validContoursWithDataWord,validContoursWithDataLine;         // we will fill these shortly

    cv::Mat matClassificationFloats;      // we will read the classification numbers into this variable as though it is a vector

    cv::FileStorage fsClassifications(classPath, cv::FileStorage::READ);        // open the classifications file

    if (fsClassifications.isOpened() == false) {                                                    // if the file was not opened successfully
        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error, unable to open training classifications file, exiting program\n\n");    // show error message
        return(0);                                                                                  // and exit program
    }
    fsClassifications["classifications"] >> matClassificationFloats;      // read classifications section into Mat classifications variable
    fsClassifications.release();                                        // close the classifications file

    cv::Mat matTrainingImages;         // we will read multiple images into this single image variable as though it is a vector

    cv::FileStorage fsTrainingImages(imagePath, cv::FileStorage::READ);          // open the training images file

    if (fsTrainingImages.isOpened() == false) {                                                 // if the file was not opened successfully
        __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error, unable to open training images file, exiting program\n\n");         // show error message
        return(0);                                                                              // and exit program
    }

    fsTrainingImages["images"] >> matTrainingImages;           // read images section into Mat training images variable
    fsTrainingImages.release();
                                                // close the traning images file
    cv::KNearest kNearest=cv::KNearest();
    kNearest.train(matTrainingImages,matClassificationFloats);

    cv::Mat matTestingNumbers =mRgb.clone();
    cv::Mat imgTestingNumbers =mRgb.clone();
    cv::Mat lineMat =mRgb.clone();

    if (matTestingNumbers.empty()) {                                // if unable to open image
         __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error: image not read from file\n\n");         // show error message on command line
        return(0);                                                  // and exit program
    }

    cv::Mat matBlurred;
    cv::Mat matThresh;
    cv::Mat matThreshCopy;

    cv::Mat imgBlurred;
    cv::Mat imgThresh;
    cv::Mat imgThreshCopy;

    cv::Mat lineBlurred;
    cv::Mat lineThresh;
    cv::Mat lineThreshCopy;

    cv::cvtColor(matTestingNumbers,matThresh, CV_RGB2GRAY);
    cv::cvtColor(imgTestingNumbers,imgThresh, CV_RGB2GRAY);
    cv::cvtColor(lineMat,lineThresh, CV_RGB2GRAY);

    matThreshCopy = matThresh.clone();
    imgThreshCopy = imgThresh.clone();
    lineThreshCopy = lineThresh.clone();

    std::vector<std::vector<cv::Point> > ptContours,ptContoursWord,ptContoursLine;
    std::vector<cv::Vec4i> v4iHierarchy,v4iHierarchyWord,v4iHierarchyLine;

    cv::findContours(matThreshCopy,ptContours,v4iHierarchy,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
    int totalWidth=0;
    for (int i = 0; i < ptContours.size(); i++) {               // for each contour
        ContourWithData contourWithData;                                                    // instantiate a contour with data object
        contourWithData.ptContour = ptContours[i];                                          // assign contour to contour with data
        contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);         // get the bounding rect
        contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);               // calculate the contour area
        if(contourWithData.fltArea >MIN_CONTOUR_AREA){
            validContoursWithData.push_back(contourWithData);
            totalWidth+=contourWithData.fltArea;
        }
    }
    int dilationValue=(float(totalWidth/validContoursWithData.size()))*0.04;
    Mat element = Mat::ones(1,dilationValue, CV_8UC1);
    cv::Size s = imgThresh.size();
    int rows = s.height;
    int cols = s.width;
    cv::dilate(imgThresh,imgThresh, element);

    imgThreshCopy = imgThresh.clone();

    cv::findContours(imgThreshCopy,ptContoursWord,v4iHierarchyWord,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
    for (int i = 0; i < ptContoursWord.size(); i++) {               // for each contour
                    ContourWithData contourWithData;                                                    // instantiate a contour with data object
                    contourWithData.ptContour = ptContoursWord[i];                                          // assign contour to contour with data
                    contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);         // get the bounding rect
                    contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);               // calculate the contour area
                    if(contourWithData.fltArea >MIN_CONTOUR_AREA*10)
                        validContoursWithDataWord.push_back(contourWithData);
    }
    Mat kernel = Mat::ones(1,dilationValue*5, CV_8UC1);
    cv::dilate(lineThresh,lineThresh,kernel);

    lineThreshCopy = lineThresh.clone();
    cv::findContours(lineThreshCopy,ptContoursLine,v4iHierarchyLine,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
    for (int i = 0; i < ptContoursLine.size(); i++) {               // for each contour
                    ContourWithData contourWithData;                                                    // instantiate a contour with data object
                    contourWithData.ptContour = ptContoursLine[i];                                          // assign contour to contour with data
                    contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);
                    contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);
                    if(contourWithData.fltArea >MIN_CONTOUR_AREA*50)
                         validContoursWithDataLine.push_back(contourWithData);                                   // add contour with data object to list of all contours with data
    }

    std::wofstream out(dataPath,std::ios::out);
    out<<wchar_t(L"");
    out.close();

    //Line and space algorithm

    std::sort(validContoursWithDataLine.begin(),validContoursWithDataLine.end(), ContourWithData::sortByBoundingRectYPosition);
    for(int m=0;m<validContoursWithDataLine.size();m++)
    {
        std::vector<ContourWithData> validLine;
        for(int l=0;l<validContoursWithDataWord.size();l++)
        {
             if(checkBounds(validContoursWithDataLine[m].boundingRect,validContoursWithDataWord[l].boundingRect))
                  validLine.push_back(validContoursWithDataWord[l]);

        }
        std::sort(validLine.begin(),validLine.end(), ContourWithData::sortByBoundingRectXPosition);
        for (int i = 0; i < validLine.size(); i++) {
                     std::vector<ContourWithData> validLetters;
                     cv::Rect boundingRect = validLine[i].boundingRect;
                     for(int j=0;j<validContoursWithData.size();j++)
                     {
                           if(checkBounds(boundingRect,validContoursWithData[j].boundingRect))
                                validLetters.push_back(validContoursWithData[j]);

                     }
                     std::sort(validLetters.begin(),validLetters.end(), ContourWithData::sortByBoundingRectXPosition);
                     for(int k=0;k<validLetters.size();k++)
                     {
                        cv::rectangle(matTestingNumbers,validLetters[k].boundingRect,cv::Scalar(0, 255, 0),0);
                        cv::Mat matROI = matThresh(validLetters[k].boundingRect);
                        cv::Mat matROIResized;
                        cv::resize(matROI, matROIResized, cv::Size(RESIZED_IMAGE_WIDTH, RESIZED_IMAGE_HEIGHT));     // resize image, this will be more consistent for recognition and storage
                        cv::Mat matROIFloat;
                        matROIResized.convertTo(matROIFloat, CV_32FC1);             // convert Mat to float, necessary for call to find_nearest
                        float fltCurrentChar =kNearest.find_nearest(matROIFloat.reshape(1,1),1);
                        std::wofstream out(dataPath,std::ios::out|std::ios::app);
                        out<<wchar_t(int(fltCurrentChar));
                        out.close();
                     }
                     std::wofstream out(dataPath,std::ios::out|std::ios::app);
                     out<<wchar_t(32);
                     out.close();
        }
    }
    mRgb=matTestingNumbers;
    /*std::wofstream out(dataPath,std::ios::out);
    out<<wchar_t(L"");
    out.close();
    for (int i = 0,j=0; i < validContoursWithData.size(); i++) {            // for each contour

                                                                // draw a green rect around the current char
        cv::rectangle(matTestingNumbers,                            // draw rectangle on original image
                      validContoursWithData[i].boundingRect,        // rect to draw
                      cv::Scalar(0, 255, 0),                        // green
                      0);                                           // thickness

        cv::Mat matROI = matThresh(validContoursWithData[i].boundingRect);          // get ROI image of bounding rect

        cv::Mat matROIResized;
        cv::resize(matROI, matROIResized, cv::Size(RESIZED_IMAGE_WIDTH, RESIZED_IMAGE_HEIGHT));     // resize image, this will be more consistent for recognition and storage

        cv::Mat matROIFloat;
        matROIResized.convertTo(matROIFloat, CV_32FC1);             // convert Mat to float, necessary for call to find_nearest
        float fltCurrentChar =kNearest.find_nearest(matROIFloat.reshape(1,1),1);


    }
*/

    return 1;
    }

    int checkBounds(Rect p,Rect c)
    {
        if (   (c.x+c.width) < (p.x+p.width)&& (c.x) > (p.x)&& (c.y) >= (p.y)&& (c.y+c.height) <= (p.y+p.height))
            return 1;
        else
            return 0;
    }
 /*  std::wofstream out(dataPath,std::ios::out);
        out<<wchar_t(L"");
        out.close();*/

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_trainIndi
    (JNIEnv *env, jclass,jlong addrRgba,jstring path,jint value){

    return 1;
    }

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_processImage
    (JNIEnv *env, jclass,jlong addrRgba){
     cv::Mat& mRgb=*(cv::Mat*)addrRgba;

     cv::Mat matTestingNumbers =mRgb;           // read in the test numbers image

     if (matTestingNumbers.empty()){                                // if unable to open image
         __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error: image not read from file\n\n");         // show error message on command line
         return(0);                                                  // and exit program
     }
     cv::Mat matGrayscale;
     cv::Mat matCanny;
     cv::Mat matBlurred;
     cv::Mat matThresh;
     cv::Mat erosionMat;
     cv::Mat matThreshCopy;
    cv::cvtColor(matTestingNumbers, matGrayscale, CV_RGB2GRAY);
    //cv::GaussianBlur(matGrayscale,matBlurred,cv::Size(5, 5),0);

    cv::threshold(matGrayscale,matThresh, 0, 255, CV_THRESH_BINARY_INV | CV_THRESH_OTSU);
    //cv::adaptiveThreshold(matSub,matThresh,255,CV_ADAPTIVE_THRESH_MEAN_C,cv::THRESH_BINARY_INV,11,2);

    mRgb=matThresh;
    return 1;
    }
JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_rotateImage
    (JNIEnv *env, jclass,jlong addrRgba,jint angle){
        cv::Mat& mRgb=*(cv::Mat*)addrRgba;
        cv::Mat dst;
        cv::Point2f pt(mRgb.cols/2., mRgb.rows/2.);
        cv::Mat r = cv::getRotationMatrix2D(pt,angle, 1.0);
        cv::warpAffine(mRgb, dst, r, Size(mRgb.cols, mRgb.rows));
        mRgb=dst;
       return 1;

}


JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_detectWords
    (JNIEnv *env, jclass, jlong addrRgba){

    return 1;
 }
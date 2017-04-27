#include <lukeentertainment_example_OpencvNativeClass.h>
const int MIN_CONTOUR_AREA = 50;

const int RESIZED_IMAGE_WIDTH = 20;
const int RESIZED_IMAGE_HEIGHT = 30;
std::string str="";
std::string strFinalString="";

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

                                    // possible chars we are interested in are digits 0 through 9 and capital letters A through Z, put these in vector intValidChars
     std::vector<int> intValidChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                                        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                                        'U', 'V', 'W', 'X', 'Y', 'Z',
                                       'a','b','c','d','e','f','g','h','i','j','k','l','m',
                                        'n','o','p','q','r','s','t','u','v','w','x','y','z'};

        imgTrainingNumbers =(cv::Mat) mRgb;         // read in training numbers image

        if (imgTrainingNumbers.empty()) {                               // if unable to open image
             __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error: image not read from file\n\n");         // show error message on command line
            return((jint)0);                                                  // and exit program
        }
        cv::cvtColor(imgTrainingNumbers, imgGrayscale, CV_RGB2GRAY);        // convert to grayscale

        cv::GaussianBlur(imgGrayscale,imgBlurred,cv::Size(5,5),0);
        cv::threshold(imgBlurred,imgThresh, 0, 255, CV_THRESH_BINARY_INV | CV_THRESH_OTSU);                           // constant subtracted from the mean or weighted mean

       imgThreshCopy = imgThresh.clone();          // make a copy of the thresh image, this in necessary b/c findContours modifies the image


        cv::findContours(imgThreshCopy,             // input image, make sure to use a copy since the function will modify this image in the course of finding contours
            ptContours,                             // output contours
            v4iHierarchy,                           // output hierarchy
            cv::RETR_EXTERNAL,                      // retrieve the outermost contours only
            cv::CHAIN_APPROX_SIMPLE);               // compress horizontal, vertical, and diagonal segments and leave only their end points

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

std::vector<ContourWithData> allContoursWithData;           // declare empty vectors,
    std::vector<ContourWithData> validContoursWithData;         // we will fill these shortly

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

    cv::Mat matTestingNumbers =mRgb;           // read in the test numbers image

    if (matTestingNumbers.empty()) {                                // if unable to open image
         __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error: image not read from file\n\n");         // show error message on command line
        return(0);                                                  // and exit program
    }

    cv::Mat matGrayscale;           //
    cv::Mat matCanny;
    cv::Mat matBlurred;             // declare more image variables
    cv::Mat matThresh;              //
    cv::Mat erosionMat;
    cv::Mat matThreshCopy;          //
    cv::cvtColor(matTestingNumbers,matThresh, CV_RGB2GRAY);
    matThreshCopy = matThresh.clone();              // make a copy of the thresh image, this in necessary b/c findContours modifies the image
    std::vector<std::vector<cv::Point> > ptContours;        // declare a vector for the contours
    std::vector<cv::Vec4i> v4iHierarchy;                    // declare a vector for the hierarchy (we won't use this in this program but this may be helpful for reference)
    cv::findContours(matThreshCopy,             // input image, make sure to use a copy since the function will modify this image in the course of finding contours
        ptContours,                             // output contours
        v4iHierarchy,                           // output hierarchy
        CV_RETR_EXTERNAL,                      // retrieve the outermost contours only
        CV_CHAIN_APPROX_SIMPLE);               // compress horizontal, vertical, and diagonal segments and leave only their end points

    for (int i = 0; i < ptContours.size(); i++) {               // for each contour
        ContourWithData contourWithData;                                                    // instantiate a contour with data object
        contourWithData.ptContour = ptContours[i];                                          // assign contour to contour with data
        contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);         // get the bounding rect
        contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);               // calculate the contour area
        if(contourWithData.fltArea >MIN_CONTOUR_AREA)
        {
            validContoursWithData.push_back(contourWithData);
        }
    }

    for(int i=0,j=0;i<validContoursWithData.size();j++)
    {

         if((validContoursWithData[j+1].boundingRect.y+validContoursWithData[j+1].boundingRect.height)<validContoursWithData[j].boundingRect.y)
         {   std::sort(validContoursWithData.begin()+i, validContoursWithData.begin()+j+1, ContourWithData::sortByBoundingRectXPosition);
            i=j+1;

         }else if(j==(validContoursWithData.size()-1)){
            std::sort(validContoursWithData.begin()+i, validContoursWithData.begin()+j+1, ContourWithData::sortByBoundingRectXPosition);

            break;
         }

    }
    int arrPos[1000];

    for(int j=0;j<validContoursWithData.size()-1;j++)
    {
        int diff=abs((validContoursWithData[j+1].boundingRect.x+validContoursWithData[j+1].boundingRect.width)-validContoursWithData[j].boundingRect.x);
        arrPos[j]=diff;
    }


     std::wofstream out(dataPath,std::ios::out);
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
        std::wofstream out(dataPath,std::ios::out|std::ios::app);
        out<<wchar_t(int(fltCurrentChar));
        out.close();
         if((validContoursWithData[i+1].boundingRect.y+validContoursWithData[i+1].boundingRect.height)<validContoursWithData[i].boundingRect.y)
          {   j=i+1;
              //strFinalString = strFinalString + "\n";
              //t=t+L"\n";
              std::wofstream out(dataPath,std::ios::out|std::ios::app);
              out<<wchar_t(10);
              out.close();
          }else if(i==(validContoursWithData.size()-1)){
              std::sort(validContoursWithData.begin()+j, validContoursWithData.begin()+i+1, ContourWithData::sortByBoundingRectXPosition);
              break;
          }

        if(arrPos[i]>10)
        {
            //t=t+L" ";
            //strFinalString = strFinalString +" ";
            std::wofstream out(dataPath,std::ios::out|std::ios::app);
            out<<wchar_t(32);
            out.close();
         }

    }

    mRgb=matTestingNumbers;

    return 1;
    }

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
    cv::GaussianBlur(matGrayscale,matBlurred,cv::Size(5, 5),0);
    cv::Mat matSub=matBlurred-matGrayscale;
    cv::threshold(matBlurred,matThresh, 0, 255, CV_THRESH_BINARY_INV | CV_THRESH_OTSU);
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

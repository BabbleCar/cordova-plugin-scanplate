var OpenALPR = {

    /**
     * Scan license plate with OpenALPR
     *
     * @param filepath Path to image
     * @param success callback function on success
     * @param error callback function on failure.
     * @returns array licenseplate matches
    */
    scan: function (filepath, success, error) {
        cordova.exec(success,error,'OpenALPR','scan',[filepath]);
    },
    init: function (success, error) {
        cordova.exec(success,error,'OpenALPR','init',[])
    }
};

module.exports = OpenALPR;

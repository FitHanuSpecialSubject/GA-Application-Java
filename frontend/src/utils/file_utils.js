export const validateExcelFile = (file) => {
  const extension = file.name.split(".").pop();
  return extension === "xlsx" || extension === "xlsm";
};
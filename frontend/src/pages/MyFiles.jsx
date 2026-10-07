import React, { useEffect, useState } from "react";
import DashboardLayout from "../layout/DashboardLayout";
import {
  Copy,
  Delete,
  Download,
  Eye,
  File,
  FileIcon,
  FileText,
  Globe,
  Globe2,
  Globe2Icon,
  Grid,
  Image,
  List,
  Lock,
  Music,
  Trash2,
  Video,
} from "lucide-react";
import { useAuth } from "@clerk/react";
import axios from "axios";
import toast from "react-hot-toast";
import { Link, useNavigate } from "react-router-dom";
import FileCard from "../components/FileCard";
import { apiEndpoints } from "../util/apiEndpoints";
import ConfirmationDialog from "../components/ConfirmationDialog";
import LinkShareModal from "../components/LinkShareModal";

const MyFiles = () => {
  const [files, setFiles] = useState([]);
  const [viewMode, setViewMode] = useState("list");
  const { getToken } = useAuth();
  const navigate = useNavigate();
  const [deleteConfirmation, setDeleteConfirmation] = useState({
    isOpen: false,
    fileId: null,
  });
  const [shareModal, setShareModal] = useState({
    isOpen: false,
    fileId: null,
    link: "",
  });

  // Fetches file for logged user
  const fetchFiles = async () => {
    try {
      const token = await getToken();
      const response = await axios.get(apiEndpoints.FETCH_FILES, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.status === 200) {
        setFiles(response.data);
      }
      console.log(token);
    } catch (error) {
      console.log("Error fetching files from server: ", error);
      toast.error("Error fetching files from server: ", error.message);
    }
  };

  // Toggle status of file to public/private
  const togglePublic = async (fileToUpdate) => {
    try {
      const token = await getToken();
      await axios.patch(
        apiEndpoints.TOGGLE_FILE(fileToUpdate.id),
        {},
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      setFiles(
        files.map((file) =>
          file.id === fileToUpdate.id
            ? { ...file, public: !file.public }
            : file,
        ),
      );
    } catch (error) {
      console.error("Error toggling file status!", error);
      toast.error("Error toggling file status: ", error.message);
    }
  };

  const handleDownload = async (file) => {
    try {
      const token = await getToken();
      const response = await axios.get(apiEndpoints.DOWNLOAD_FILE(file.id), {
        headers: { Authorization: `Bearer ${token}` },
        responseType: "blob", // blob is binary large object like img, audio, video
      });

      // create blob url and trigger download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url; // add link
      link.setAttribute("download", file.name);
      document.body.appendChild(link); // add link as child
      link.click(); // call link to download
      link.remove(); // remove link
      window.URL.revokeObjectURL(url); // clean up the object url
    } catch (error) {
      console.error("Download failed!", error);
      toast.error("Error downloading file: ", error.message);
    }
  };

  // Closes delete confirmation modal
  const closeDeleteConfirmation = () => {
    setDeleteConfirmation({
      isOpen: false,
      fileId: null,
    });
  };

  // Opens delete confirmation modal
  const openDeleteConfirmation = (fileId) => {
    setDeleteConfirmation({
      isOpen: true,
      fileId,
    });
  };

  // Opens share link modal
  const openShareModal = (fileId) => {
    const link = `${window.location.origin}/file/${fileId}`;
    setShareModal({
      isOpen: true,
      fileId,
      link,
    });
  };

  // close share link modal
  const closeShareModal = () => {
    setShareModal({
      isOpen: false,
      fileId: null,
      link: "",
    });
  };

  //Delete file after confirmation
  const handleDelete = async () => {
    const fileId = deleteConfirmation.fileId;
    if (!fileId) return;

    try {
      const token = await getToken();
      const response = await axios.delete(apiEndpoints.DELETE_FILE(fileId), {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.status === 204) {
        setFiles(files.filter((file) => file.id !== fileId));
        closeDeleteConfirmation();
        toast.success("Successfully Deleted File!");
      } else {
        toast.error("Error Deleting File!");
      }
    } catch (error) {
      console.error("Error deleting file!", error);
      toast.error("Error deleting file: ", error.message);
    }
  };

  useEffect(() => {
    fetchFiles();
  }, [getToken]);

  const getFileIcon = (file) => {
    const extension = file.name.split(".").pop().toLowerCase();

    if (["jpg", "jpeg", "png", "gif", "svg", "webp"].includes(extension)) {
      return <Image size={24} className="text-blue-500" />;
    }

    if (["mp4", "webm", "mov", "avi", "mkv"].includes(extension)) {
      return <Video size={24} className="text-blue-500" />;
    }

    if (["mp3", "wav", "ogg", "flac", "m4a"].includes(extension)) {
      return <Music size={24} className="text-green-500" />;
    }

    if (["pdf", "doc", "docx", "txt", "rtf"].includes(extension)) {
      return <FileText size={24} className="text-amber-500" />;
    }

    return <FileIcon size={24} className="text-blue-500" />;
  };

  return (
    <DashboardLayout activeMenu="My Files">
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">My Files {files.length}</h2>
          <div className="flex items-center gap-3">
            <List
              onClick={() => setViewMode("list")}
              size={24}
              className={`cursor-pointer transition-colors ${viewMode === "list" ? "text-blue-600" : "text-gray-400 hover:text-gray-600"}`}
            />
            <Grid
              size={24}
              onClick={() => setViewMode("grid")}
              className={`cursor-pointer transition-colors ${viewMode === "grid" ? "text-blue-600" : "text-gray-400 hover:text-gray-600"}`}
            />
          </div>
        </div>

        {files.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-12 flex flex-col items-center justify-center">
            <File size={60} className="text-blue-300 mb-4" />
            <h3 className="text-xl font-medium text-gray-700 mb-2">
              No files uploaded yet
            </h3>
            <p className="text-gray-500 text-center max-w-md mb-6">
              Start uploading files to see them listed here. You can upload
              documents, images, and other files to share and manage them
              securely.
            </p>
            <button
              onClick={() => navigate("/upload")}
              className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors cursor-pointer"
            >
              Go to Upload
            </button>
          </div>
        ) : viewMode === "grid" ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {files.map((file) => (
              <FileCard
                key={file.id}
                file={file}
                onDelete={openDeleteConfirmation}
                onTogglePublic={togglePublic}
                onDownload={handleDownload}
                onShareLink={openShareModal}
              />
            ))}
          </div>
        ) : (
          <div className="overflow-x-auto bg-white rounded-lg shadow">
            <table className="min-w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Size
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Uploaded
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Sharing
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {files.map((file) => (
                  <tr
                    key={file.id}
                    className="hover:bg-gray-50 transition-colors"
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-800">
                      <div className="flex items-center gap-2">
                        {getFileIcon(file)}
                        {file.name}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-800">
                      {(file.size / 1024).toFixed(1)} KB
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-800">
                      {new Date(file.uploadedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-800">
                      <div className="flex items-center gap-4">
                        <button
                          onClick={() => togglePublic(file)}
                          className="flex items-center gap-2 cursor-pointer group"
                        >
                          {file.public ? (
                            <>
                              <Globe2Icon
                                size={16}
                                className="text-green-600"
                              />
                              <span className="group-hover:underline">
                                Public
                              </span>
                            </>
                          ) : (
                            <>
                              <Lock size={16} className="text-gray-600" />
                              <span className="group-hover:underline">
                                Private
                              </span>
                            </>
                          )}
                        </button>
                        {file.public && (
                          <button
                            onClick={() => openShareModal(file.id)}
                            className="flex items-center gap-2 cursor-pointer group"
                          >
                            <Copy size={16} />
                            <span className="group-hover:underline">
                              Share Link
                            </span>
                          </button>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="grid grid-cols-3 gap-4">
                        <div className="flex justify-center">
                          <button
                            onClick={() => handleDownload(file)}
                            title="Download"
                            className="text-gray-500 hover:text-blue-600 cursor-pointer"
                          >
                            <Download size={18} />
                          </button>
                        </div>
                        <div className="flex justify-center">
                          <button
                            onClick={() => openDeleteConfirmation(file.id)}
                            title="Delete"
                            className="text-gray-500 hover:text-red-600 cursor-pointer"
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                        <div className="flex justify-center">
                          {file.public ? (
                            <a
                              href={`/file/${file.id}`}
                              title="View File"
                              target="_blank"
                              rel="noreferrer"
                              className="text-gray-500 hover:text-blue-600"
                            >
                              <Eye size={18} />
                            </a>
                          ) : (
                            <span className="w-4.5"></span>
                          )}
                        </div>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <ConfirmationDialog
          isOpen={deleteConfirmation.isOpen}
          onClose={closeDeleteConfirmation}
          title="Delete File"
          message="Are you sure want to delete this file? This action cannot be undone."
          confirmText="Delete"
          cancelText="Cancel"
          onConfirm={handleDelete}
          confirmButtonClass="bg-red-600 hover:bg-red-700"
        />

        <LinkShareModal
          isOpen={shareModal.isOpen}
          onClose={closeShareModal}
          link={shareModal.link}
          title="Share File"
        />
      </div>
    </DashboardLayout>
  );
};

export default MyFiles;

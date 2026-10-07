import { useAuth } from "@clerk/react";
import axios from "axios";
import React, {
  createContext,
  useCallback,
  useEffect,
  useEffectEvent,
  useState,
} from "react";
import toast from "react-hot-toast";
import { apiEndpoints } from "../util/apiEndpoints";

// create and initialise context
export const UserCreditsContext = createContext();

export const UserCreditsProvider = ({ children }) => {
  const [credits, setCredits] = useState(5);
  const [loading, setLoading] = useState(false);
  const { getToken, isSignedIn } = useAuth();

  // function to fetch user credits that can be called from anywhere
  const fetchUserCredits = useCallback(async () => {
    if (!isSignedIn) return;

    setLoading(true);

    try {
      const token = await getToken();
      const response = await axios.get(apiEndpoints.GET_CREDITS, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.status === 200) {
        setCredits(response.data.credits);
      } else {
        toast.error("Unable to get the credits!");
      }
    } catch (error) {
      console.error("Error fetching the user credits!", error);
    } finally {
      setLoading(false);
    }
  }, [getToken, isSignedIn]);

  useEffect(() => {
    if (isSignedIn) fetchUserCredits();
  }, [fetchUserCredits, isSignedIn]);

  const updateCredits = useCallback((newCredits) => {
    console.log("Updating the credits", newCredits);
    setCredits(newCredits);
  }, []);

  // make context available through application
  const contextValue = {
    credits,
    setCredits,
    fetchUserCredits,
    updateCredits,
  };

  return (
    // wrap all child components and supply values
    <UserCreditsContext.Provider value={contextValue}>
      {" "}
      {children}
    </UserCreditsContext.Provider>
  );
};

import React from "react";
import { useClerk, useUser } from "@clerk/react";
import { useNavigate } from "react-router-dom";

import HeroSection from "../components/landing/HeroSection";
import FeaturesSection from "../components/landing/FeaturesSection";
import PricingSection from "../components/landing/PricingSection";
import TestimonialsSection from "../components/landing/TestimonialsSection";
import CTASection from "../components/landing/CTASection";
import Footer from "../components/landing/Footer";

import { features, pricingPlans, testimonials } from "../assets/data";

const Landing = () => {
  const { openSignIn, openSignUp } = useClerk();
  const { isLoaded, isSignedIn } = useUser();
  const navigate = useNavigate();

  const handleSignIn = () => {
    if (!isLoaded) return;

    if (isSignedIn) {
      navigate("/dashboard");
      return;
    }

    openSignIn({
      fallbackRedirectUrl: "/dashboard",
    });
  };

  const handleSignUp = () => {
    if (!isLoaded) return;

    if (isSignedIn) {
      navigate("/dashboard");
      return;
    }

    openSignUp({
      fallbackRedirectUrl: "/dashboard",
    });
  };

  return (
    <div className="landing-page bg-linear-to-b from-gray-50 to-gray-100">
      <HeroSection
        openSignIn={handleSignIn}
        openSignUp={handleSignUp}
        isSignedIn={isSignedIn}
      />

      <FeaturesSection features={features} />

      <PricingSection
        pricingPlans={pricingPlans}
        openSignUp={handleSignUp}
        isSignedIn={isSignedIn}
      />

      {/* <TestimonialsSection testimonials={testimonials} /> */}

      <CTASection
        openSignUp={handleSignUp}
        isSignedIn={isSignedIn}
      />

      <Footer />
    </div>
  );
};

export default Landing;